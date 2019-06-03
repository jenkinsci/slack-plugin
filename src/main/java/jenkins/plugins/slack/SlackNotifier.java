package jenkins.plugins.slack;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.HostnameRequirement;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.Project;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.config.GlobalCredentialMigrator;
import jenkins.plugins.slack.logging.BuildAwareLogger;
import jenkins.plugins.slack.logging.BuildKey;
import jenkins.plugins.slack.logging.SlackNotificationsLogger;
import jenkins.plugins.slack.matrix.MatrixTriggerMode;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.POST;

import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

public class SlackNotifier extends Notifier {

    public static final String MATRIX_PROJECT_CLASS_NAME = "hudson.matrix.MatrixProject";
    public static final String MATRIX_RUN_CLASS_NAME = "hudson.matrix.MatrixRun";
    private static final Logger logger = Logger.getLogger(SlackNotifier.class.getName());

    private String baseUrl;
    private String teamDomain;
    private String authToken;
    private String tokenCredentialId;
    private boolean botUser;
    private String room;
    private String sendAs;
    private String iconEmoji;
    private String username;
    private boolean startNotification;
    private boolean notifySuccess;
    private boolean notifyAborted;
    private boolean notifyNotBuilt;
    private boolean notifyUnstable;
    private boolean notifyRegression;
    private boolean notifyFailure;
    private boolean notifyEveryFailure;
    private boolean notifyBackToNormal;
    private boolean notifyRepeatedFailure;
    private boolean includeTestSummary;
    private boolean includeFailedTests;
    private MatrixTriggerMode matrixTriggerMode;
    private CommitInfoChoice commitInfoChoice;
    private boolean includeCustomMessage;
    private String customMessage;
    private String customMessageSuccess;
    private String customMessageAborted;
    private String customMessageNotBuilt;
    private String customMessageUnstable;
    private String customMessageFailure;

    /** @deprecated use {@link #tokenCredentialId} */
    @SuppressWarnings("DeprecatedIsStillUsed")
    private transient String authTokenCredentialId;

    public String getAuthTokenCredentialId() {
        return tokenCredentialId;
    }

    @SuppressWarnings("deprecation")
    private Object readResolve() {
        if (this.authTokenCredentialId != null) {
            this.tokenCredentialId = authTokenCredentialId;
        }
        return this;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getTeamDomain() {
        return teamDomain;
    }

    @DataBoundSetter
    public void setTeamDomain(final String teamDomain) {
        this.teamDomain = teamDomain;
    }

    public String getRoom() {
        return room;
    }

    @DataBoundSetter
    public void setRoom(String room) {
        this.room = room;
    }

    public String getAuthToken() {
        return authToken;
    }

    @DataBoundSetter
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getTokenCredentialId() {
        return tokenCredentialId;
    }

    @DataBoundSetter
    public void setTokenCredentialId(String tokenCredentialId) {
        this.tokenCredentialId = tokenCredentialId;
    }

    public boolean getBotUser() {
        return botUser;
    }

    @DataBoundSetter
    public void setBotUser(boolean botUser) {
        this.botUser = botUser;
    }

    public String getSendAs() {
        return sendAs;
    }

    @DataBoundSetter
    public void setSendAs(String sendAs) {
        this.sendAs = sendAs;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    @DataBoundSetter
    public void setIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
    }

    public String getUsername() {
        return username;
    }

    @DataBoundSetter
    public void setUsername(String username) {
        this.username = username;
    }

    public boolean getStartNotification() {
        return startNotification;
    }

    public boolean getNotifySuccess() {
        return notifySuccess;
    }

    public CommitInfoChoice getCommitInfoChoice() {
        return commitInfoChoice;
    }

    @CheckForNull
    public MatrixTriggerMode getMatrixTriggerMode() {
        return matrixTriggerMode;
    }

    public boolean getNotifyAborted() {
        return notifyAborted;
    }

    public boolean getNotifyFailure() {
        return notifyFailure;
    }

    public boolean getNotifyEveryFailure() {
        return notifyEveryFailure;
    }

    public boolean getNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    public boolean getNotifyUnstable() {
        return notifyUnstable;
    }

    public boolean getNotifyRegression() {
        return notifyRegression;
    }

    public boolean getNotifyBackToNormal() {
        return notifyBackToNormal;
    }

    public boolean getIncludeTestSummary() {
        return includeTestSummary;
    }

    public boolean getIncludeFailedTests() {
        return includeFailedTests;
    }

    public boolean getNotifyRepeatedFailure() {
        return notifyRepeatedFailure;
    }

    public boolean getIncludeCustomMessage() {
        return includeCustomMessage;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public String getCustomMessageSuccess() {
        return customMessageSuccess;
    }

    public String getCustomMessageAborted() {
        return customMessageAborted;
    }

    public String getCustomMessageNotBuilt() {
        return customMessageNotBuilt;
    }

    public String getCustomMessageUnstable() {
        return customMessageUnstable;
    }

    public String getCustomMessageFailure() {
        return customMessageFailure;
    }

    @DataBoundSetter
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @DataBoundSetter
    public void setStartNotification(boolean startNotification) {
        this.startNotification = startNotification;
    }

    @DataBoundSetter
    public void setNotifySuccess(boolean notifySuccess) {
        this.notifySuccess = notifySuccess;
    }

    @DataBoundSetter
    public void setCommitInfoChoice(CommitInfoChoice commitInfoChoice) {
        this.commitInfoChoice = commitInfoChoice;
    }

    @DataBoundSetter
    public void setMatrixTriggerMode(MatrixTriggerMode matrixTriggerMode) {
        this.matrixTriggerMode = matrixTriggerMode;
    }

    @DataBoundSetter
    public void setNotifyAborted(boolean notifyAborted) {
        this.notifyAborted = notifyAborted;
    }

    @DataBoundSetter
    public void setNotifyFailure(boolean notifyFailure) {
        this.notifyFailure = notifyFailure;
    }

    @DataBoundSetter
    public void setNotifyEveryFailure(boolean notifyEveryFailure) {
        this.notifyEveryFailure = notifyEveryFailure;
    }

    @DataBoundSetter
    public void setNotifyNotBuilt(boolean notifyNotBuilt) {
        this.notifyNotBuilt = notifyNotBuilt;
    }

    @DataBoundSetter
    public void setNotifyUnstable(boolean notifyUnstable) {
        this.notifyUnstable = notifyUnstable;
    }

    @DataBoundSetter
    public void setNotifyRegression(boolean notifyRegression) {
        this.notifyRegression = notifyRegression;
    }

    @DataBoundSetter
    public void setNotifyBackToNormal(boolean notifyBackToNormal) {
        this.notifyBackToNormal = notifyBackToNormal;
    }

    @DataBoundSetter
    public void setIncludeTestSummary(boolean includeTestSummary) {
        this.includeTestSummary = includeTestSummary;
    }

    @DataBoundSetter
    public void setIncludeFailedTests(boolean includeFailedTests) {
        this.includeFailedTests = includeFailedTests;
    }

    @DataBoundSetter
    public void setNotifyRepeatedFailure(boolean notifyRepeatedFailure) {
        this.notifyRepeatedFailure = notifyRepeatedFailure;
    }

    @DataBoundSetter
    public void setIncludeCustomMessage(boolean includeCustomMessage) {
        this.includeCustomMessage = includeCustomMessage;
    }

    @DataBoundSetter
    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    @DataBoundSetter
    public void setCustomMessageSuccess(String customMessageSuccess) {
        this.customMessageSuccess = customMessageSuccess;
    }

    @DataBoundSetter
    public void setCustomMessageAborted(String customMessageAborted) {
        this.customMessageAborted = customMessageAborted;
    }

    @DataBoundSetter
    public void setCustomMessageNotBuilt(String customMessageNotBuilt) {
        this.customMessageNotBuilt = customMessageNotBuilt;
    }

    @DataBoundSetter
    public void setCustomMessageUnstable(String customMessageUnstable) {
        this.customMessageUnstable = customMessageUnstable;
    }

    @DataBoundSetter
    public void setCustomMessageFailure(String customMessageFailure) {
        this.customMessageFailure = customMessageFailure;
    }

    @DataBoundConstructor
    public SlackNotifier(CommitInfoChoice commitInfoChoice) {
        this.commitInfoChoice = commitInfoChoice;
    }

    @Deprecated
    public SlackNotifier(final String baseUrl, final String teamDomain, final String authToken, final boolean botUser, final String room, final String tokenCredentialId,
                         final String sendAs, final boolean startNotification, final boolean notifyAborted, final boolean notifyFailure,
                         final boolean notifyNotBuilt, final boolean notifySuccess, final boolean notifyUnstable, final boolean notifyRegression, final boolean notifyBackToNormal,
                         final boolean notifyRepeatedFailure, final boolean includeTestSummary, final boolean includeFailedTests,
                         CommitInfoChoice commitInfoChoice, boolean includeCustomMessage, String customMessage) {
        this(
                baseUrl, teamDomain, authToken, botUser, room, tokenCredentialId, sendAs, startNotification,
                notifyAborted, notifyFailure, notifyNotBuilt, notifySuccess, notifyUnstable, notifyRegression,
                notifyBackToNormal, notifyRepeatedFailure, includeTestSummary, includeFailedTests,
                commitInfoChoice, includeCustomMessage, customMessage, null, null, null, null, null
        );
    }

    @Deprecated
    public SlackNotifier(final String baseUrl, final String teamDomain, final String authToken, final boolean botUser, final String room, final String tokenCredentialId,
                         final String sendAs, final boolean startNotification, final boolean notifyAborted, final boolean notifyFailure,
                         final boolean notifyNotBuilt, final boolean notifySuccess, final boolean notifyUnstable, final boolean notifyRegression, final boolean notifyBackToNormal,
                         final boolean notifyRepeatedFailure, final boolean includeTestSummary, final boolean includeFailedTests,
                         CommitInfoChoice commitInfoChoice, boolean includeCustomMessage, String customMessage, String customMessageSuccess,
                         String customMessageAborted, String customMessageNotBuilt, String customMessageUnstable, String customMessageFailure) {
        this(
                baseUrl, teamDomain, authToken, botUser, room, tokenCredentialId, sendAs, startNotification,
                notifyAborted, notifyFailure, notifyNotBuilt, notifySuccess, notifyUnstable, notifyRegression,
                notifyBackToNormal, notifyRepeatedFailure, includeTestSummary, includeFailedTests, MatrixTriggerMode.ONLY_CONFIGURATIONS,
                commitInfoChoice, includeCustomMessage, customMessage, customMessageSuccess, customMessageAborted, customMessageNotBuilt,
                customMessageUnstable, customMessageFailure
        );
    }

    @Deprecated
    public SlackNotifier(final String baseUrl, final String teamDomain, final String authToken, final boolean botUser, final String room, final String tokenCredentialId,
                         final String sendAs, final boolean startNotification, final boolean notifyAborted, final boolean notifyFailure,
                         final boolean notifyNotBuilt, final boolean notifySuccess, final boolean notifyUnstable, final boolean notifyRegression, final boolean notifyBackToNormal,
                         final boolean notifyRepeatedFailure, final boolean includeTestSummary, final boolean includeFailedTests, MatrixTriggerMode matrixTriggerMode,
                         CommitInfoChoice commitInfoChoice, boolean includeCustomMessage, String customMessage, String customMessageSuccess,
                         String customMessageAborted, String customMessageNotBuilt, String customMessageUnstable, String customMessageFailure) {

        new SlackNotifier.SlackNotifierBuilder()
                .withBaseUrl(baseUrl)
                .withTeamDomain(teamDomain)
                .withAuthToken(authToken)
                .withBotUser(botUser)
                .withRoom(room)
                .withTokenCredentialId(tokenCredentialId)
                .withSendAs(sendAs)
                .withIconEmoji("")
                .withUsername("")
                .withStartNotification(startNotification)
                .withNotifyAborted(notifyAborted)
                .withNotifyFailure(notifyFailure)
                .withNotifyNotBuilt(notifyNotBuilt)
                .withNotifySuccess(notifySuccess)
                .withNotifyUnstable(notifyUnstable)
                .withNotifyRegression(notifyRegression)
                .withNotifyBackToNormal(notifyBackToNormal)
                .withNotifyRepeatedFailure(notifyRepeatedFailure)
                .withIncludeTestSummary(includeTestSummary)
                .withIncludeFailedTests(includeFailedTests)
                .withMatrixTriggerMode(matrixTriggerMode)
                .withCommitInfoChoice(commitInfoChoice)
                .withIncludeCustomMessage(includeCustomMessage)
                .withCustomMessage(customMessage)
                .withCustomMessageSuccess(customMessageSuccess)
                .withCustomMessageAborted(customMessageAborted)
                .withCustomMessageNotBuilt(customMessageNotBuilt)
                .withCustomMessageUnstable(customMessageUnstable)
                .withCustomMessageFailure(customMessageFailure)
                .build();
    }

    public SlackNotifier(SlackNotifierBuilder slackNotifierBuilder) {
        this.baseUrl = slackNotifierBuilder.baseUrl;
        if(this.baseUrl != null && !this.baseUrl.isEmpty() && !this.baseUrl.endsWith("/")) {
            this.baseUrl += "/";
        }
        this.teamDomain = slackNotifierBuilder.teamDomain;
        this.authToken = slackNotifierBuilder.authToken;
        this.tokenCredentialId = slackNotifierBuilder.tokenCredentialId;
        this.botUser = slackNotifierBuilder.botUser;
        this.room = slackNotifierBuilder.room;
        this.sendAs = slackNotifierBuilder.sendAs;
        this.iconEmoji = slackNotifierBuilder.iconEmoji;
        this.username = slackNotifierBuilder.username;
        this.startNotification = slackNotifierBuilder.startNotification;
        this.notifySuccess = slackNotifierBuilder.notifySuccess;
        this.notifyAborted = slackNotifierBuilder.notifyAborted;
        this.notifyNotBuilt = slackNotifierBuilder.notifyNotBuilt;
        this.notifyAborted = slackNotifierBuilder.notifyAborted;
        this.notifyNotBuilt = slackNotifierBuilder.notifyNotBuilt;
        this.notifyUnstable = slackNotifierBuilder.notifyUnstable;
        this.notifyRegression = slackNotifierBuilder.notifyRegression;
        this.notifyFailure = slackNotifierBuilder.notifyFailure;
        this.notifyEveryFailure = slackNotifierBuilder.notifyEveryFailure;
        this.notifyBackToNormal = slackNotifierBuilder.notifyBackToNormal;
        this.notifyRepeatedFailure = slackNotifierBuilder.notifyRepeatedFailure;
        this.includeTestSummary = slackNotifierBuilder.includeTestSummary;
        this.includeFailedTests = slackNotifierBuilder.includeFailedTests;
        this.matrixTriggerMode = slackNotifierBuilder.matrixTriggerMode;
        this.commitInfoChoice = slackNotifierBuilder.commitInfoChoice;
        this.includeCustomMessage = slackNotifierBuilder.includeCustomMessage;
        if (includeCustomMessage) {
            this.customMessage = slackNotifierBuilder.customMessage;
            this.customMessageSuccess = slackNotifierBuilder.customMessageSuccess;
            this.customMessageAborted = slackNotifierBuilder.customMessageAborted;
            this.customMessageNotBuilt = slackNotifierBuilder.customMessageNotBuilt;
            this.customMessageUnstable = slackNotifierBuilder.customMessageUnstable;
            this.customMessageFailure = slackNotifierBuilder.customMessageFailure;
        } else {
            this.customMessage = null;
        }
    }

    private static class SlackNotifierBuilder {
        private String baseUrl;
        private String teamDomain;
        private String authToken;
        private String tokenCredentialId;
        private boolean botUser;
        private String room;
        private String sendAs;
        private String iconEmoji;
        private String username;
        private boolean startNotification;
        private boolean notifySuccess;
        private boolean notifyAborted;
        private boolean notifyNotBuilt;
        private boolean notifyUnstable;
        private boolean notifyRegression;
        private boolean notifyFailure;
        private boolean notifyEveryFailure;
        private boolean notifyBackToNormal;
        private boolean notifyRepeatedFailure;
        private boolean includeTestSummary;
        private boolean includeFailedTests;
        private MatrixTriggerMode matrixTriggerMode;
        private CommitInfoChoice commitInfoChoice;
        private boolean includeCustomMessage;
        private String customMessage;
        private String customMessageSuccess;
        private String customMessageAborted;
        private String customMessageNotBuilt;
        private String customMessageUnstable;
        private String customMessageFailure;

        public SlackNotifierBuilder() { }

        public SlackNotifierBuilder withBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public SlackNotifierBuilder withTeamDomain(String teamDomain) {
            this.teamDomain = teamDomain;
            return this;
        }

        public SlackNotifierBuilder withAuthToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public SlackNotifierBuilder withBotUser(boolean botUser) {
            this.botUser = botUser;
            return this;
        }

        public SlackNotifierBuilder withRoom(String room) {
            this.room = room;
            return this;
        }

        public SlackNotifierBuilder withSendAs(String sendAs) {
            this.sendAs = sendAs;
            return this;
        }

        public SlackNotifierBuilder withIconEmoji(String iconEmoji) {
            this.iconEmoji = iconEmoji;
            return this;
        }

        public SlackNotifierBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public SlackNotifierBuilder withTokenCredentialId(String tokenCredentialId) {
            this.tokenCredentialId = tokenCredentialId;
            return this;
        }

        public SlackNotifierBuilder withStartNotification(boolean startNotification) {
            this.startNotification = startNotification;
            return this;
        }

        public SlackNotifierBuilder withNotifyAborted(boolean notifyAborted) {
            this.notifyAborted = notifyAborted;
            return this;
        }

        public SlackNotifierBuilder withNotifyNotBuilt(boolean notifyNotBuilt) {
            this.notifyNotBuilt = notifyNotBuilt;
            return this;
        }

        public SlackNotifierBuilder withNotifySuccess(boolean notifySuccess) {
            this.notifySuccess = notifySuccess;
            return this;
        }

        public SlackNotifierBuilder withNotifyUnstable(boolean notifyUnstable) {
            this.notifyUnstable = notifyUnstable;
            return this;
        }

        public SlackNotifierBuilder withNotifyRegression(boolean notifyRegression) {
            this.notifyRegression = notifyRegression;
            return this;
        }

        public SlackNotifierBuilder withNotifyFailure(boolean notifyFailure) {
            this.notifyFailure = notifyFailure;
            return this;
        }

        public SlackNotifierBuilder withNotifyEveryFailure(boolean notifyEveryFailure) {
            this.notifyEveryFailure = notifyEveryFailure;
            return this;
        }

        public SlackNotifierBuilder withNotifyBackToNormal(boolean notifyBackToNormal) {
            this.notifyBackToNormal = notifyBackToNormal;
            return this;
        }

        public SlackNotifierBuilder withNotifyRepeatedFailure(boolean notifyRepeatedFailure) {
            this.notifyRepeatedFailure = notifyRepeatedFailure;
            return this;
        }

        public SlackNotifierBuilder withIncludeTestSummary(boolean includeTestSummary) {
            this.includeTestSummary = includeTestSummary;
            return this;
        }

        public SlackNotifierBuilder withIncludeFailedTests(boolean includeFailedTests) {
            this.includeFailedTests = includeFailedTests;
            return this;
        }

        public SlackNotifierBuilder withMatrixTriggerMode(MatrixTriggerMode matrixTriggerMode) {
            this.matrixTriggerMode = matrixTriggerMode;
            return this;
        }

        public SlackNotifierBuilder withCommitInfoChoice(CommitInfoChoice commitInfoChoice) {
            this.commitInfoChoice = commitInfoChoice;
            return this;
        }

        public SlackNotifierBuilder withIncludeCustomMessage(boolean includeCustomMessage) {
            this.includeCustomMessage = includeCustomMessage;
            return this;
        }

        public SlackNotifierBuilder withCustomMessage(String customMessage) {
            this.customMessage = customMessage;
            return this;
        }

        public SlackNotifierBuilder withCustomMessageSuccess(String customMessageSuccess) {
            this.customMessageSuccess = customMessageSuccess;
            return this;
        }

        public SlackNotifierBuilder withCustomMessageAborted(String customMessageAborted) {
            this.customMessageAborted = customMessageAborted;
            return this;
        }

        public SlackNotifierBuilder withCustomMessageNotBuilt(String customMessageNotBuilt) {
            this.customMessageNotBuilt = customMessageNotBuilt;
            return this;
        }

        public SlackNotifierBuilder withCustomMessageUnstable(String customMessageUnstable) {
            this.customMessageUnstable = customMessageUnstable;
            return this;
        }

        public SlackNotifierBuilder withCustomMessageFailure(String customMessageFailure) {
            this.customMessageFailure = customMessageFailure;
            return this;
        }

        public SlackNotifier build() {
            return new SlackNotifier(this);
        }
    }

    public boolean isAnyCustomMessagePopulated() {
        return Stream.of(
                customMessage,
                customMessageSuccess,
                customMessageAborted,
                customMessageNotBuilt,
                customMessageUnstable,
                customMessageFailure
        ).anyMatch(StringUtils::isNotEmpty);

    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public SlackService newSlackService(AbstractBuild abstractBuild, BuildListener listener) {
        DescriptorImpl descriptor = getDescriptor();
        String teamDomain = Util.fixEmpty(this.teamDomain) != null ? this.teamDomain : descriptor.getTeamDomain();
        String baseUrl = Util.fixEmpty(this.baseUrl) != null ? this.baseUrl : descriptor.getBaseUrl();
        String authToken = Util.fixEmpty(this.authToken);
        boolean botUser = this.botUser || descriptor.isBotUser();
        String authTokenCredentialId = Util.fixEmpty(this.tokenCredentialId) != null ? this.tokenCredentialId :
                descriptor.getTokenCredentialId();
        String room = Util.fixEmpty(this.room) != null ? this.room : descriptor.getRoom();
        String iconEmoji = Util.fixEmpty(this.iconEmoji) != null ? this.iconEmoji : descriptor.getIconEmoji();
        String username = Util.fixEmpty(this.username) != null ? this.username : descriptor.getUsername();

        EnvVars env;
        try {
            env = abstractBuild.getEnvironment(listener);
        } catch (Exception e) {
            listener.getLogger().println("Error retrieving environment vars: " + e.getMessage());
            env = new EnvVars();
        }
        baseUrl = env.expand(baseUrl);
        teamDomain = env.expand(teamDomain);
        authToken = env.expand(authToken);
        authTokenCredentialId = env.expand(authTokenCredentialId);
        room = env.expand(room);
        final String populatedToken = CredentialsObtainer.getTokenToUse(authTokenCredentialId, abstractBuild.getParent(), authToken);
        return new StandardSlackService(baseUrl, teamDomain, botUser, room, false, iconEmoji, username, populatedToken);
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        String buildKey = BuildKey.format(build);
        BuildAwareLogger log = createLogger(listener);
        log.debug(buildKey, "Performing complete notifications");
        JenkinsTokenExpander tokenExpander = new JenkinsTokenExpander(listener);
        try {
            new ActiveNotifier(this, slackFactory(listener), log, tokenExpander).completed(build);
            if (notifyRegression) {
                log.debug(buildKey, "Performing finalize notifications");
                new ActiveNotifier(this, slackFactory(listener), log, tokenExpander).finalized(build);
            }
        } catch (Exception e) {
            log.info(buildKey,"Exception attempting Slack notification: " + e.getMessage());
        }
        return true;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        String buildKey = BuildKey.format(build);
        BuildAwareLogger log = createLogger(listener);
        try {
            if (startNotification) {
                log.debug(buildKey, "Performing start notifications");
                new ActiveNotifier(this, slackFactory(listener), log, new JenkinsTokenExpander(listener)).started(build);
            }
        } catch (Exception e) {
            log.info(buildKey,"Exception attempting Slack notification: " + e.getMessage());
        }
        return super.prebuild(build, listener);
    }

    private Function<AbstractBuild<?, ?>, SlackService> slackFactory(BuildListener listener) {
        return b -> newSlackService(b, listener);
    }

    private static BuildAwareLogger createLogger(BuildListener listener) {
        return new SlackNotificationsLogger(logger, listener.getLogger());
    }

    public boolean isMatrixRun(AbstractBuild<?, ?> build) {
        return build.getClass().getName().equals(MATRIX_RUN_CLASS_NAME);
    }

    @Extension @Symbol("slackNotifier")
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public static final String PLUGIN_DISPLAY_NAME = "Slack Notifications";
        private String baseUrl;
        private String teamDomain;
        private String token;
        private String tokenCredentialId;
        private boolean botUser;
        private String iconEmoji;
        private String username;
        private String room;
        private String sendAs;

        public DescriptorImpl() {
            load();
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        @DataBoundSetter
        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getTeamDomain() {
            return teamDomain;
        }

        @DataBoundSetter
        public void setTeamDomain(String teamDomain) {
            this.teamDomain = teamDomain;
        }

        /**
         * Deprecated for removal in 3.0
         *
         * Use tokenCredentialId instead
         */
        @Deprecated
        public String getToken() {
            return token;
        }

        /**
         * Deprecated for removal in 3.0
         *
         * Use tokenCredentialId instead
         */
        @Deprecated
        @DataBoundSetter
        public void setToken(String token) {
            this.token = token;
        }

        public String getTokenCredentialId() {
            return tokenCredentialId;
        }

        @DataBoundSetter
        public void setTokenCredentialId(String tokenCredentialId) {
            this.tokenCredentialId = tokenCredentialId;
        }

        public boolean isBotUser() {
            return botUser;
        }

        @Deprecated
        public boolean getBotUser() {
            return botUser;
        }

        @DataBoundSetter
        public void setBotUser(boolean botUser) {
            this.botUser = botUser;
        }

        public String getIconEmoji() { return iconEmoji; }

        @DataBoundSetter
        public void setIconEmoji(String iconEmoji) {
            this.iconEmoji = iconEmoji;
        }

        public String getUsername() { return username; }

        @DataBoundSetter
        public void setUsername(String username) {
            this.username = username;
        }

        public String getRoom() {
            return room;
        }

        @DataBoundSetter
        public void setRoom(String room) {
            this.room = room;
        }

        public String getSendAs() {
            return sendAs;
        }

        @DataBoundSetter
        public void setSendAs(String sendAs) {
            this.sendAs = sendAs;
        }

        public ListBoxModel doFillCommitInfoChoiceItems() {
            ListBoxModel model = new ListBoxModel();

            for (CommitInfoChoice choice : CommitInfoChoice.values()) {
                model.add(choice.getDisplayName(), choice.name());
            }

            return model;
        }

        public ListBoxModel doFillTokenCredentialIdItems(@AncestorInPath Item context) {

            Jenkins jenkins = Jenkins.get();

            if(context == null && !jenkins.hasPermission(Jenkins.ADMINISTER) ||
                    context != null && !context.hasPermission(Item.EXTENDED_READ)) {
                return new StandardListBoxModel();
            }

            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withAll(lookupCredentials(
                            StringCredentials.class,
                            context,
                            ACL.SYSTEM,
                            new HostnameRequirement("*.slack.com"))
                    );
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) {
            req.bindJSON(this, formData);
            save();
            return true;
        }

        /**
         * @deprecated  use {@link #getSlackService(String, String, String, boolean, String, Item)} instead}
         */
        @Deprecated
        SlackService getSlackService(final String baseUrl, final String teamDomain, final String authTokenCredentialId, final boolean botUser, final String roomId) {
            return getSlackService(baseUrl, teamDomain, authTokenCredentialId, botUser, roomId, null);
        }

        SlackService getSlackService(final String baseUrl, final String teamDomain, final String authTokenCredentialId, final boolean botUser, final String roomId, final Item item) {
            final String populatedToken = CredentialsObtainer.getTokenToUse(authTokenCredentialId, item,null );
            if (populatedToken != null) {
                return new StandardSlackService(baseUrl, teamDomain, botUser, roomId, false, null, null, populatedToken);
            } else {
                throw new NoSuchElementException("Could not obtain credentials with credential id: " + authTokenCredentialId);
            }
        }

        @SuppressWarnings("unused") // called by jelly
        public boolean isMatrixProject(AbstractProject<?, ?> project) {
            return project.getClass().getName().equals(MATRIX_PROJECT_CLASS_NAME);
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return PLUGIN_DISPLAY_NAME;
        }

        @POST
        public FormValidation doTestConnection(@QueryParameter("baseUrl") final String baseUrl,
                                               @QueryParameter("teamDomain") final String teamDomain,
                                               @QueryParameter("tokenCredentialId") final String tokenCredentialId,
                                               @QueryParameter("botUser") final boolean botUser,
                                               @QueryParameter("room") final String room,
                                               @AncestorInPath Project project) {
            if (project == null) {
                Jenkins.get().checkPermission(Jenkins.ADMINISTER);
            } else {
                project.checkPermission(Item.CONFIGURE);
            }

            try {
                String targetUrl = baseUrl;
                if(targetUrl != null && !targetUrl.isEmpty() && !targetUrl.endsWith("/")) {
                    targetUrl += "/";
                }

                // override with values from global config if fields aren't set
                if (StringUtils.isEmpty(targetUrl)) {
                    targetUrl = this.baseUrl;
                }
                String targetDomain = Util.fixEmpty(teamDomain) != null ? teamDomain : this.teamDomain;
                boolean targetBotUser = botUser || this.botUser;
                String targetTokenCredentialId = Util.fixEmpty(tokenCredentialId) != null ? tokenCredentialId :
                        this.tokenCredentialId;
                String targetRoom = Util.fixEmpty(room) != null ? room : this.room;

                SlackService testSlackService = getSlackService(targetUrl, targetDomain, targetTokenCredentialId, targetBotUser, targetRoom, project);
                String message = "Slack/Jenkins plugin: you're all set on " + DisplayURLProvider.get().getRoot();
                boolean success = testSlackService.publish(message, "good");
                return success ? FormValidation.ok("Success") : FormValidation.error("Failure");
            } catch (Exception e) {
                logger.log(Level.WARNING, "Slack config form validation error", e);
                return FormValidation.error("Client error : " + e.getMessage());
            }
        }

        private Object readResolve() {
            if (Util.fixEmpty(this.token) != null) {
                this.tokenCredentialId = new GlobalCredentialMigrator().migrate(this.token).getId();
                this.token = null;

                save();
            }

            return this;
        }
    }

}
