package jenkins.plugins.slack;

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.AffectedFile;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.TestResult;
import hudson.triggers.SCMTrigger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.decisions.Context;
import jenkins.plugins.slack.decisions.NotificationConditions;
import jenkins.plugins.slack.logging.BuildAwareLogger;
import jenkins.plugins.slack.logging.BuildKey;
import jenkins.plugins.slack.matrix.MatrixTriggerMode;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;

@SuppressWarnings("rawtypes")
public class ActiveNotifier implements FineGrainedNotifier {
    SlackNotifier notifier;
    private final Function<AbstractBuild<?, ?>, SlackService> slackFactory;
    private final BuildAwareLogger log;
    private final TokenExpander tokenExpander;

    public ActiveNotifier(SlackNotifier notifier, Function<AbstractBuild<?, ?>, SlackService> slackFactory, BuildAwareLogger log, TokenExpander tokenExpander) {
        super();
        this.notifier = notifier;
        this.slackFactory = slackFactory;
        this.log = log;
        this.tokenExpander = tokenExpander;
    }

    public void deleted(AbstractBuild r) {
    }

    public void started(AbstractBuild build) {
        if (skipOnMatrixChildren(build)) {
            return;
        }
        String key = BuildKey.format(build);

        CauseAction causeAction = build.getAction(CauseAction.class);

        if (causeAction != null) {
            Cause scmCause = causeAction.findCause(SCMTrigger.SCMTriggerCause.class);
            if (scmCause == null) {
                log.debug(key, "was not caused by SCM Trigger");
                MessageBuilder message = new MessageBuilder(notifier, build, log, tokenExpander);
                message.append(causeAction.getCauses().get(0).getShortDescription());
                message.appendOpenLink();
                if (notifier.getIncludeCustomMessage()) {
                  message.appendCustomMessage(build.getResult());
                }
                notifyStart(build, message.toString());
                // Cause was found, exit early to prevent double-message
                return;
            }
        } else {
            log.debug(key, "did not have a cause action");
        }

        String changes = getChanges(build, notifier.getIncludeCustomMessage());
        if (changes != null) {
            notifyStart(build, changes);
        } else {
            notifyStart(build, getBuildStatusMessage(build, false, false, notifier.getIncludeCustomMessage()));
        }
    }

    private void notifyStart(AbstractBuild build, String message) {
        AbstractProject<?, ?> project = build.getProject();
        AbstractBuild<?, ?> lastBuild = project.getLastBuild();
        SlackService slack = slackFactory.apply(build);
        if (lastBuild != null) {
            AbstractBuild<?, ?> previousBuild = lastBuild.getPreviousCompletedBuild();
            if (previousBuild == null) {
                slack.publish(message, "good");
            } else {
                slack.publish(message, getBuildColor(previousBuild));
            }
        } else {
            slack.publish(message, "good");
        }
    }

    public void finalized(AbstractBuild r) {
    }

    public void completed(AbstractBuild r) {
        if (skipOnMatrixChildren(r)) {
            return;
        }
        String key = BuildKey.format(r);
        AbstractProject<?, ?> project = r.getProject();
        AbstractBuild<?, ?> previousBuild = project.getLastBuild();
        if (null != previousBuild) {
            do {
                previousBuild = previousBuild.getPreviousCompletedBuild();
            } while ((null != previousBuild && previousBuild.getResult() == Result.ABORTED) || (null != previousBuild && previousBuild.getNumber() == r.getNumber()));
            if (null != previousBuild) {
                log.info(key, "found #%d as previous completed, non-aborted build", previousBuild.getNumber());
            } else {
                log.debug(key, "did not find previous completed, non-aborted build");
            }

            NotificationConditions conditions = NotificationConditions.create(notifier, log);
            if (conditions.test(new Context(r, previousBuild))) {
                String message = getBuildStatusMessage(r, notifier.getIncludeTestSummary(),
                        notifier.getIncludeFailedTests(), notifier.getIncludeCustomMessage());
                if (notifier.getCommitInfoChoice().showAnything()) {
                    message = message + "\n" + getCommitList(r);
                }
                slackFactory.apply(r).publish(message, getBuildColor(r));
                if (notifier.getUploadFiles()) {
                    slackFactory.apply(r).upload(r.getWorkspace(), notifier.getArtifactIncludes(), log.getTaskListener());
                }
            }
        }
    }

    private boolean skipOnMatrixChildren(AbstractBuild build) {
        if (notifier.isMatrixRun(build)) {
            MatrixTriggerMode matrixTriggerMode = notifier.getMatrixTriggerMode();
            return !(matrixTriggerMode != null && matrixTriggerMode.forChild);
        }
        return false;
    }

    String getChanges(AbstractBuild r, boolean includeCustomMessage) {
        String key = BuildKey.format(r);
        if (!r.hasChangeSetComputed()) {
            log.debug(key, "did not have change set computed");
            return null;
        }
        ChangeLogSet changeSet = r.getChangeSet();
        List<Entry> entries = new LinkedList<>();
        Set<AffectedFile> files = new HashSet<>();
        for (Object o : changeSet.getItems()) {
            Entry entry = (Entry) o;
            log.debug(key, "adding changeset entry: %s", o);
            entries.add(entry);
            if (CollectionUtils.isNotEmpty(entry.getAffectedFiles())) {
                files.addAll(entry.getAffectedFiles());
            }
        }
        if (entries.isEmpty()) {
            log.debug(key, "did not have entries in changeset");
            return null;
        }
        Set<String> authors = new HashSet<>();
        for (Entry entry : entries) {
            authors.add(entry.getAuthor().getDisplayName());
        }
        MessageBuilder message = new MessageBuilder(notifier, r, log, tokenExpander);
        message.append("Started by changes from ");
        message.append(StringUtils.join(authors, ", "));
        message.append(" (");
        message.append(files.size());
        message.append(" file(s) changed)");
        message.appendOpenLink();
        if (includeCustomMessage) {
            message.appendCustomMessage(r.getResult());
        }
        return message.toString();
    }

    String getCommitList(AbstractBuild r) {
        String buildKey = BuildKey.format(r);
        ChangeLogSet changeSet = r.getChangeSet();
        List<Entry> entries = new LinkedList<>();
        for (Object o : changeSet.getItems()) {
            Entry entry = (Entry) o;
            log.debug(buildKey, "adding changeset entry: %s", o);
            entries.add(entry);
        }
        if (entries.isEmpty()) {
            log.debug(buildKey, "did not have entries in changeset");
            Cause.UpstreamCause c = (Cause.UpstreamCause)r.getCause(Cause.UpstreamCause.class);
            if (c == null) {
                return "No Changes.";
            }
            String upProjectName = c.getUpstreamProject();
            int buildNumber = c.getUpstreamBuild();
            AbstractProject project = Jenkins.get().getItemByFullName(upProjectName, AbstractProject.class);
            if (project != null) {
                AbstractBuild upBuild = project.getBuildByNumber(buildNumber);
                return getCommitList(upBuild);
            }
        }
        Set<String> commits = new HashSet<>();
        for (Entry entry : entries) {
            StringBuilder commit = new StringBuilder();
            CommitInfoChoice commitInfoChoice = notifier.getCommitInfoChoice();
            if (commitInfoChoice.showTitle()) {
                commit.append(entry.getMsg());
            }
            if (commitInfoChoice.showAuthor()) {
                commit.append(" [").append(entry.getAuthor().getDisplayName()).append("]");
            }
            commits.add(commit.toString());
        }
        MessageBuilder message = new MessageBuilder(notifier, r, log, tokenExpander);
        message.append("Changes:\n- ");
        message.append(StringUtils.join(commits, "\n- "));
        return message.toString();
    }

    static String getBuildColor(AbstractBuild r) {
        Result result = r.getResult();
        if (result == Result.SUCCESS) {
            return "good";
        } else if (result == Result.FAILURE) {
            return "danger";
        } else {
            return "warning";
        }
    }

    String getBuildStatusMessage(AbstractBuild r, boolean includeTestSummary, boolean includeFailedTests, boolean includeCustomMessage) {
        MessageBuilder message = new MessageBuilder(notifier, r, log, tokenExpander);
        message.appendStatusMessage();
        message.appendDuration();
        message.appendOpenLink();
        if (includeTestSummary) {
            message.appendTestSummary();
        }
        if (includeFailedTests) {
            message.appendFailedTests();
        }
        if (includeCustomMessage) {
            message.appendCustomMessage(r.getResult());
        }
        return message.toString();
    }

    public static class MessageBuilder {

        private static final Pattern aTag = Pattern.compile("(?i)<a([^>]+)>(.+?)</a>|([{%])");
        private static final Pattern href = Pattern.compile("\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))");
        private static final String BACK_TO_NORMAL_STATUS_MESSAGE = "Back to normal",
                                    STILL_FAILING_STATUS_MESSAGE = "Still Failing",
                                    SUCCESS_STATUS_MESSAGE = "Success",
                                    FAILURE_STATUS_MESSAGE = "Failure",
                                    ABORTED_STATUS_MESSAGE = "Aborted",
                                    NOT_BUILT_STATUS_MESSAGE = "Not built",
                                    UNSTABLE_STATUS_MESSAGE = "Unstable",
                                    REGRESSION_STATUS_MESSAGE = "Regression",
                                    UNKNOWN_STATUS_MESSAGE = "Unknown";

        private StringBuilder message;
        private SlackNotifier notifier;
        private final BuildAwareLogger log;
        private final String buildKey;
        private final TokenExpander tokenExpander;
        private AbstractBuild build;

        public MessageBuilder(SlackNotifier notifier, AbstractBuild build, BuildAwareLogger log, TokenExpander tokenExpander) {
            this.notifier = notifier;
            this.log = log;
            this.tokenExpander = tokenExpander;
            this.message = new StringBuilder();
            this.build = build;
            this.buildKey = BuildKey.format(build);
            startMessage();
        }

        public MessageBuilder appendStatusMessage() {
            message.append(this.escape(getStatusMessage(build)));
            return this;
        }

        private String getStatusMessage(AbstractBuild r) {
            Result result = r.getResult();
            Result previousResult;
            if(null != result) {
                AbstractBuild lastBuild = r.getProject().getLastBuild();
                if (lastBuild != null) {
                    Run previousBuild = lastBuild.getPreviousBuild();
                    Run previousSuccessfulBuild = r.getPreviousSuccessfulBuild();
                    boolean buildHasSucceededBefore = previousSuccessfulBuild != null;

                    /*
                     * If the last build was aborted, go back to find the last non-aborted build.
                     * This is so that aborted builds do not affect build transitions.
                     * I.e. if build 1 was failure, build 2 was aborted and build 3 was a success the transition
                     * should be failure -> success (and therefore back to normal) not aborted -> success.
                     */
                    Run lastNonAbortedBuild = previousBuild;
                    while (lastNonAbortedBuild != null && lastNonAbortedBuild.getResult() == Result.ABORTED) {
                        lastNonAbortedBuild = lastNonAbortedBuild.getPreviousBuild();
                    }


                    /* If all previous builds have been aborted, then use
                     * SUCCESS as a default status so an aborted message is sent
                     */
                    if (lastNonAbortedBuild == null) {
                        previousResult = Result.SUCCESS;
                    } else {
                        previousResult = lastNonAbortedBuild.getResult();
                    }

                    /* Back to normal should only be shown if the build has actually succeeded at some point.
                     * Also, if a build was previously unstable and has now succeeded the status should be
                     * "Back to normal"
                     */
                    if (result == Result.SUCCESS
                            && (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE)
                            && buildHasSucceededBefore && notifier.getNotifyBackToNormal()) {
                        return BACK_TO_NORMAL_STATUS_MESSAGE;
                    }
                    if (result == Result.FAILURE && previousResult == Result.FAILURE) {
                        return STILL_FAILING_STATUS_MESSAGE;
                    }
                    if (result == Result.SUCCESS) {
                        return SUCCESS_STATUS_MESSAGE;
                    }
                    if (result == Result.FAILURE) {
                        return FAILURE_STATUS_MESSAGE;
                    }
                    if (result == Result.ABORTED) {
                        return ABORTED_STATUS_MESSAGE;
                    }
                    if (result == Result.NOT_BUILT) {
                        return NOT_BUILT_STATUS_MESSAGE;
                    }
                    if (result == Result.UNSTABLE) {
                        return UNSTABLE_STATUS_MESSAGE;
                    }
                    if (lastNonAbortedBuild != null && previousResult != null && result.isWorseThan(previousResult)) {
                        return REGRESSION_STATUS_MESSAGE;
                    }
                }
            }
            return UNKNOWN_STATUS_MESSAGE;
        }

        public MessageBuilder append(String string) {
            message.append(this.escape(string));
            return this;
        }

        public MessageBuilder append(Object string) {
            message.append(this.escape(string.toString()));
            return this;
        }

        private MessageBuilder startMessage() {
            message.append(this.escape(build.getProject().getFullDisplayName()));
            message.append(" - ");
            message.append(this.escape(build.getDisplayName()));
            message.append(" ");
            return this;
        }

        public MessageBuilder appendOpenLink() {
            String url = DisplayURLProvider.get().getRunURL(build);
            message.append(" (<").append(url).append("|Open>)");
            return this;
        }

        public MessageBuilder appendDuration() {
            message.append(" after ");
            String durationString;
            if(message.toString().contains(BACK_TO_NORMAL_STATUS_MESSAGE)){
                durationString = createBackToNormalDurationString();
            } else {
                durationString = build.getDurationString();
            }
            message.append(durationString);
            return this;
        }

        public MessageBuilder appendTestSummary() {
            AbstractTestResultAction<?> action = this.build
                    .getAction(AbstractTestResultAction.class);
            if (action != null) {
                int total = action.getTotalCount();
                int failed = action.getFailCount();
                int skipped = action.getSkipCount();
                message.append("\nTest Status:\n");
                message.append("\tPassed: ")
                        .append(total - failed - skipped);
                message.append(", Failed: ").append(failed);
                message.append(", Skipped: ").append(skipped);
            } else {
                message.append("\nNo Tests found.");
            }
            return this;
        }

        public MessageBuilder appendFailedTests() {
            AbstractTestResultAction<?> action = this.build
                    .getAction(AbstractTestResultAction.class);
            if (action != null) {
                int failed = action.getFailCount();
                if (failed > 0) {
                    message.append("\n").append(failed).append(" Failed Tests:\n");
                    for(TestResult result : action.getFailedTests()) {
                        message.append("\t").append(getTestClassAndMethod(result)).append(" after ")
                                .append(result.getDurationString()).append("\n");
                    }
                }
            }
            return this;
        }

        public MessageBuilder appendCustomMessage(Result buildResult) {
            String customMessage = "";
            if (buildResult != null) {
                if (buildResult == Result.SUCCESS) {
                    customMessage = notifier.getCustomMessageSuccess();
                } else if (buildResult == Result.ABORTED) {
                    customMessage = notifier.getCustomMessageAborted();
                } else if (buildResult == Result.NOT_BUILT) {
                    customMessage = notifier.getCustomMessageNotBuilt();
                } else if (buildResult == Result.UNSTABLE) {
                    customMessage = notifier.getCustomMessageUnstable();
                } else if (buildResult == Result.FAILURE) {
                    customMessage = notifier.getCustomMessageFailure();
                }
            }
            if (customMessage == null || customMessage.isEmpty()) {
                customMessage = notifier.getCustomMessage();
            }
            String replaced = tokenExpander.expand(customMessage, build);
            message.append("\n");
            message.append(replaced);
            return this;
        }

        private String getTestClassAndMethod(TestResult result) {
            String fullDisplayName = result.getFullDisplayName();

            if (StringUtils.countMatches(fullDisplayName, ".") > 1) {
                int methodDotIndex = fullDisplayName.lastIndexOf('.');
                int testClassDotIndex = fullDisplayName.substring(0, methodDotIndex).lastIndexOf('.');

                return fullDisplayName.substring(testClassDotIndex + 1);

            } else {
                return fullDisplayName;
            }
        }

        private String createBackToNormalDurationString(){
            // This status code guarantees that the previous build fails and has been successful before
            // The back to normal time is the time since the build first broke
            Run previousSuccessfulBuild = build.getPreviousSuccessfulBuild();
            if (null != previousSuccessfulBuild && null != previousSuccessfulBuild.getNextBuild()) {
                Run initialFailureAfterPreviousSuccessfulBuild = previousSuccessfulBuild.getNextBuild();
                if (initialFailureAfterPreviousSuccessfulBuild != null) {
                    long initialFailureStartTime = initialFailureAfterPreviousSuccessfulBuild.getStartTimeInMillis();
                    long initialFailureDuration = initialFailureAfterPreviousSuccessfulBuild.getDuration();
                    long initialFailureEndTime = initialFailureStartTime + initialFailureDuration;
                    long buildStartTime = build.getStartTimeInMillis();
                    long buildDuration = build.getDuration();
                    long buildEndTime = buildStartTime + buildDuration;
                    long backToNormalDuration = buildEndTime - initialFailureEndTime;
                    return Util.getTimeSpanString(backToNormalDuration);
                }
            }
            return null;
        }

        private String escapeCharacters(String string) {
            string = string.replace("&", "&amp;");
            string = string.replace("<", "&lt;");
            string = string.replace(">", "&gt;");

            return string;
        }

        private String[] extractReplaceLinks(Matcher aTag, StringBuffer sb) {
            int size = 0;
            List<String> links = new ArrayList<>();
            while (aTag.find()) {
                String firstGroup = aTag.group(1);
                if (firstGroup != null) {
                    Matcher url = href.matcher(firstGroup);
                    if (url.find()) {
                        String escapeThis = aTag.group(3);
                        if (escapeThis != null) {
                            aTag.appendReplacement(sb, String.format("{%s}", size++));
                            links.add(escapeThis);
                        } else {
                            aTag.appendReplacement(sb, String.format("{%s}", size++));
                            links.add(String.format("<%s|%s>", url.group(1).replaceAll("\"", ""), aTag.group(2)));
                        }
                    }
                } else {
                    String escapeThis = aTag.group(3);
                    aTag.appendReplacement(sb, String.format("{%s}", size++));
                    links.add(escapeThis);
                }
            }
            aTag.appendTail(sb);
            return links.toArray(new String[size]);
        }

        public String escape(String string) {
            StringBuffer pattern = new StringBuffer();
            Object[] links = extractReplaceLinks(aTag.matcher(string), pattern);
            return MessageFormat.format(escapeCharacters(pattern.toString()), links);
        }

        public String toString() {
            return message.toString();
        }
    }
}
