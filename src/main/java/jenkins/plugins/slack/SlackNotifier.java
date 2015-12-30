package jenkins.plugins.slack;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.HostnameRequirement;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.model.Descriptor;
import hudson.model.listeners.ItemListener;
import hudson.model.Result;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.config.ItemConfigMigrator;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

public class SlackNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(SlackNotifier.class.getName());

    private String teamDomain;
    private String authToken;
    private String authTokenCredentialId;
    private boolean botUser;
    private String room;
    private String sendAs;
    private boolean startNotification;
    private boolean notifySuccess;
    private boolean notifyAborted;
    private boolean notifyNotBuilt;
    private boolean notifyUnstable;
    private boolean notifyFailure;
    private boolean notifyBackToNormal;
    private boolean notifyRepeatedFailure;
    private boolean includeTestSummary;
    private boolean includeFailedTests;
    private CommitInfoChoice commitInfoChoice;
    private boolean includeCustomMessage;
    private String customMessage;
    private Map<String, String> customMessageMap = new HashMap<String, String>();

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
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

    public String getAuthTokenCredentialId() {
        return authTokenCredentialId;
    }

    @DataBoundSetter
    public void setAuthTokenCredentialId(String authTokenCredentialId) {
        this.authTokenCredentialId = authTokenCredentialId;
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

    public boolean getStartNotification() {
        return startNotification;
    }

    public boolean getNotifySuccess() {
        return notifySuccess;
    }

    public CommitInfoChoice getCommitInfoChoice() {
        return commitInfoChoice;
    }

    public boolean getNotifyAborted() {
        return notifyAborted;
    }

    public boolean getNotifyFailure() {
        return notifyFailure;
    }

    public boolean getNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    public boolean getNotifyUnstable() {
        return notifyUnstable;
    }

    public boolean getNotifyBackToNormal() {
        return notifyBackToNormal;
    }

    public boolean includeTestSummary() {
        return includeTestSummary;
    }

    public boolean includeFailedTests() {
        return includeFailedTests;
    }

    public boolean getNotifyRepeatedFailure() {
        return notifyRepeatedFailure;
    }

    public boolean includeCustomMessage() {
        return includeCustomMessage;
    }

    public String getCustomMessage() {
        return customMessage;
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
    public void setNotifyAborted(boolean notifyAborted) {
        this.notifyAborted = notifyAborted;
    }

    @DataBoundSetter
    public void setNotifyFailure(boolean notifyFailure) {
        this.notifyFailure = notifyFailure;
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
    public void setNotifyBackToNormal(boolean notifyBackToNormal) {
        this.notifyBackToNormal = notifyBackToNormal;
    }

    @DataBoundSetter
    public void setIncludeTestSummary(boolean includeTestSummary) {
        this.includeTestSummary = includeTestSummary;
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
   
   /**
     * Returns a custom message for a specific build type
     * @param buildType
     * @return String
     */
    public String getCustomMessage(Result buildType) {
        return this.getCustomMessage(buildType.toString());
    }

    /**
     * Returns a custom message for a specific build type
     * @param buildType
     * @return String
     */
    public String getCustomMessage(String buildType) {
        String message = null;

        for (Map.Entry pair : customMessageMap.entrySet()) {
            if ( pair.getKey().toString().equals(buildType) )
            {
                message = pair.getValue().toString();
            }
        }

        // If there's no specific message, use the generic custom message
        if ( message == null || message.equals("") )
        {
            message = getCustomMessage();
        }
        return message;
    }

    @DataBoundConstructor
    public SlackNotifier() {
        super();
    }

    public SlackNotifier(final String teamDomain, final String authToken, final boolean botUser, final String room, final String authTokenCredentialId,
                         final String sendAs, final boolean startNotification, final boolean notifyAborted, final boolean notifyFailure,
                         final boolean notifyNotBuilt, final boolean notifySuccess, final boolean notifyUnstable, final boolean notifyBackToNormal,
                         final boolean notifyRepeatedFailure, final boolean includeTestSummary, final boolean includeFailedTests,
                         CommitInfoChoice commitInfoChoice, boolean includeCustomMessage, String customMessage, HashMap<String, String> customMessageMap) {
        super();
        this.teamDomain = teamDomain;
        this.authToken = authToken;
        this.authTokenCredentialId = StringUtils.trim(authTokenCredentialId);
        this.botUser = botUser;
        this.room = room;
        this.sendAs = sendAs;
        this.startNotification = startNotification;
        this.notifyAborted = notifyAborted;
        this.notifyFailure = notifyFailure;
        this.notifyNotBuilt = notifyNotBuilt;
        this.notifySuccess = notifySuccess;
        this.notifyUnstable = notifyUnstable;
        this.notifyBackToNormal = notifyBackToNormal;
        this.notifyRepeatedFailure = notifyRepeatedFailure;
        this.includeTestSummary = includeTestSummary;
        this.includeFailedTests = includeFailedTests;
        this.commitInfoChoice = commitInfoChoice;
        this.includeCustomMessage = includeCustomMessage;
        this.customMessage = customMessage;
        this.customMessageMap = customMessageMap;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public SlackService newSlackService(AbstractBuild r, BuildListener listener) {
        String teamDomain = this.teamDomain;
        if (StringUtils.isEmpty(teamDomain)) {
            teamDomain = getDescriptor().getTeamDomain();
        }
        String authToken = this.authToken;
        boolean botUser = this.botUser;
        if (StringUtils.isEmpty(authToken)) {
            authToken = getDescriptor().getToken();
            botUser = getDescriptor().getBotUser();
        }
        String authTokenCredentialId = this.authTokenCredentialId;
        if (StringUtils.isEmpty(authTokenCredentialId)) {
            authTokenCredentialId = getDescriptor().getTokenCredentialId();
        }
        String room = this.room;
        if (StringUtils.isEmpty(room)) {
            room = getDescriptor().getRoom();
        }

        EnvVars env = null;
        try {
            env = r.getEnvironment(listener);
        } catch (Exception e) {
            listener.getLogger().println("Error retrieving environment vars: " + e.getMessage());
            env = new EnvVars();
        }
        teamDomain = env.expand(teamDomain);
        authToken = env.expand(authToken);
        authTokenCredentialId = env.expand(authTokenCredentialId);
        room = env.expand(room);

        return new StandardSlackService(teamDomain, authToken, authTokenCredentialId, botUser, room);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return true;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        if (startNotification) {
            Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
            for (Publisher publisher : map.values()) {
                if (publisher instanceof SlackNotifier) {
                    logger.info("Invoking Started...");
                    new ActiveNotifier((SlackNotifier) publisher, listener).started(build);
                }
            }
        }
        return super.prebuild(build, listener);
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String teamDomain;
        private String token;
        private String tokenCredentialId;
        private boolean botUser;
        private String room;
        private String sendAs;

        public static final CommitInfoChoice[] COMMIT_INFO_CHOICES = CommitInfoChoice.values();

        public DescriptorImpl() {
            load();
        }

        public String getTeamDomain() {
            return teamDomain;
        }

        public String getToken() {
            return token;
        }

        public String getTokenCredentialId() {
            return tokenCredentialId;
        }

        public boolean getBotUser() {
            return botUser;
        }

        public String getRoom() {
            return room;
        }

        public String getSendAs() {
            return sendAs;
        }

        public ListBoxModel doFillTokenCredentialIdItems() {
            if (!Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER)) {
                return new ListBoxModel();
            }
            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withAll(lookupCredentials(
                            StringCredentials.class,
                            Jenkins.getInstance(),
                            ACL.SYSTEM,
                            new HostnameRequirement("*.slack.com"))
                    );
        }

        //WARN users that they should not use the plain/exposed token, but rather the token credential id
        public FormValidation doCheckToken(@QueryParameter String value) {
            //always show the warning - TODO investigate if there is a better way to handle this
            return FormValidation.warning("Exposing your Integration Token is a security risk. Please use the Integration Token Credential ID");
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public SlackNotifier newInstance(StaplerRequest sr, JSONObject json) {
            String teamDomain = sr.getParameter("slackTeamDomain");
            String token = sr.getParameter("slackToken");
            String tokenCredentialId = json.getString("tokenCredentialId");
            boolean botUser = "true".equals(sr.getParameter("slackBotUser"));
            String room = sr.getParameter("slackRoom");
            boolean startNotification = "true".equals(sr.getParameter("slackStartNotification"));
            boolean notifySuccess = "true".equals(sr.getParameter("slackNotifySuccess"));
            boolean notifyAborted = "true".equals(sr.getParameter("slackNotifyAborted"));
            boolean notifyNotBuilt = "true".equals(sr.getParameter("slackNotifyNotBuilt"));
            boolean notifyUnstable = "true".equals(sr.getParameter("slackNotifyUnstable"));
            boolean notifyFailure = "true".equals(sr.getParameter("slackNotifyFailure"));
            boolean notifyBackToNormal = "true".equals(sr.getParameter("slackNotifyBackToNormal"));
            boolean notifyRepeatedFailure = "true".equals(sr.getParameter("slackNotifyRepeatedFailure"));
            boolean includeTestSummary = "true".equals(sr.getParameter("includeTestSummary"));
            boolean includeFailedTests = "true".equals(sr.getParameter("includeFailedTests"));
            CommitInfoChoice commitInfoChoice = CommitInfoChoice.forDisplayName(sr.getParameter("slackCommitInfoChoice"));
            boolean includeCustomMessage = "on".equals(sr.getParameter("includeCustomMessage"));
            String customMessage = sr.getParameter("customMessage");

            HashMap<String,String> customMessageMap = new HashMap<String,String>();
            customMessageMap.put("SUCCESS", sr.getParameter("customMessageBuildSuccess"));
            customMessageMap.put("FAILURE", sr.getParameter("customMessageBuildFailure"));
            customMessageMap.put("ABORTED", sr.getParameter("customMessageBuildAborted"));
            customMessageMap.put("NOT_BUILT", sr.getParameter("customMessageBuildNotBuilt"));
            customMessageMap.put("UNSTABLE", sr.getParameter("customMessageBuildUnstable"));

            return new SlackNotifier(teamDomain, token, botUser, room, tokenCredentialId, sendAs, startNotification, notifyAborted,
                    notifyFailure, notifyNotBuilt, notifySuccess, notifyUnstable, notifyBackToNormal, notifyRepeatedFailure,
                    includeTestSummary, includeFailedTests, commitInfoChoice, includeCustomMessage, customMessage, customMessageMap);
        }

        @Override
        public boolean configure(StaplerRequest sr, JSONObject formData) throws FormException {
            teamDomain = sr.getParameter("slackTeamDomain");
            token = sr.getParameter("slackToken");
            tokenCredentialId = formData.getJSONObject("slack").getString("tokenCredentialId");
            botUser = "true".equals(sr.getParameter("slackBotUser"));
            room = sr.getParameter("slackRoom");
            sendAs = sr.getParameter("slackSendAs");
            save();
            return super.configure(sr, formData);
        }

        SlackService getSlackService(final String teamDomain, final String authToken, final String authTokenCredentialId, final boolean botUser, final String room) {
            return new StandardSlackService(teamDomain, authToken, authTokenCredentialId, botUser, room);
        }

        @Override
        public String getDisplayName() {
            return "Slack Notifications";
        }

        public FormValidation doTestConnection(@QueryParameter("slackTeamDomain") final String teamDomain,
                                               @QueryParameter("slackToken") final String authToken,
                                               @QueryParameter("tokenCredentialId") final String authTokenCredentialId,
                                               @QueryParameter("slackBotUser") final boolean botUser,
                                               @QueryParameter("slackRoom") final String room) throws FormException {
            try {
                String targetDomain = teamDomain;
                if (StringUtils.isEmpty(targetDomain)) {
                    targetDomain = this.teamDomain;
                }
                String targetToken = authToken;
                boolean targetBotUser = botUser;
                if (StringUtils.isEmpty(targetToken)) {
                    targetToken = this.token;
                    targetBotUser = this.botUser;
                }
                String targetTokenCredentialId = authTokenCredentialId;
                if (StringUtils.isEmpty(targetTokenCredentialId)) {
                    targetTokenCredentialId = this.tokenCredentialId;
                }
                String targetRoom = room;
                if (StringUtils.isEmpty(targetRoom)) {
                    targetRoom = this.room;
                }
                SlackService testSlackService = getSlackService(targetDomain, targetToken, targetTokenCredentialId, targetBotUser, targetRoom);
                String message = "Slack/Jenkins plugin: you're all set on " + DisplayURLProvider.get().getRoot();
                boolean success = testSlackService.publish(message, "good");
                return success ? FormValidation.ok("Success") : FormValidation.error("Failure");
            } catch (Exception e) {
                return FormValidation.error("Client error : " + e.getMessage());
            }
        }
    }

    @Deprecated
    public static class SlackJobProperty extends hudson.model.JobProperty<AbstractProject<?, ?>> {

        private String teamDomain;
        private String token;
        private boolean botUser;
        private String room;
        private boolean startNotification;
        private boolean notifySuccess;
        private boolean notifyAborted;
        private boolean notifyNotBuilt;
        private boolean notifyUnstable;
        private boolean notifyFailure;
        private boolean notifyBackToNormal;
        private boolean notifyRepeatedFailure;
        private boolean includeTestSummary;
        private boolean showCommitList;
        private boolean includeCustomMessage;
        private String customMessage;

        @DataBoundConstructor
        public SlackJobProperty(String teamDomain,
                                String token,
                                boolean botUser,
                                String room,
                                boolean startNotification,
                                boolean notifyAborted,
                                boolean notifyFailure,
                                boolean notifyNotBuilt,
                                boolean notifySuccess,
                                boolean notifyUnstable,
                                boolean notifyBackToNormal,
                                boolean notifyRepeatedFailure,
                                boolean includeTestSummary,
                                boolean showCommitList,
                                boolean includeCustomMessage,
                                String customMessage) {
            this.teamDomain = teamDomain;
            this.token = token;
            this.botUser = botUser;
            this.room = room;
            this.startNotification = startNotification;
            this.notifyAborted = notifyAborted;
            this.notifyFailure = notifyFailure;
            this.notifyNotBuilt = notifyNotBuilt;
            this.notifySuccess = notifySuccess;
            this.notifyUnstable = notifyUnstable;
            this.notifyBackToNormal = notifyBackToNormal;
            this.notifyRepeatedFailure = notifyRepeatedFailure;
            this.includeTestSummary = includeTestSummary;
            this.showCommitList = showCommitList;
            this.includeCustomMessage = includeCustomMessage;
            this.customMessage = customMessage;
        }

        @Exported
        public String getTeamDomain() {
            return teamDomain;
        }

        @Exported
        public String getToken() {
            return token;
        }

        @Exported
        public boolean getBotUser() {
            return botUser;
        }

        @Exported
        public String getRoom() {
            return room;
        }

        @Exported
        public boolean getStartNotification() {
            return startNotification;
        }

        @Exported
        public boolean getNotifySuccess() {
            return notifySuccess;
        }

        @Exported
        public boolean getShowCommitList() {
            return showCommitList;
        }

        @Override
        public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
            return super.prebuild(build, listener);
        }

        @Exported
        public boolean getNotifyAborted() {
            return notifyAborted;
        }

        @Exported
        public boolean getNotifyFailure() {
            return notifyFailure;
        }

        @Exported
        public boolean getNotifyNotBuilt() {
            return notifyNotBuilt;
        }

        @Exported
        public boolean getNotifyUnstable() {
            return notifyUnstable;
        }

        @Exported
        public boolean getNotifyBackToNormal() {
            return notifyBackToNormal;
        }

        @Exported
        public boolean includeTestSummary() {
            return includeTestSummary;
        }

        @Exported
        public boolean getNotifyRepeatedFailure() {
            return notifyRepeatedFailure;
        }

        @Exported
        public boolean includeCustomMessage() {
            return includeCustomMessage;
        }

        @Exported
        public String getCustomMessage() {
            return customMessage;
        }

    }

    @Extension(ordinal = 100) public static final class Migrator extends ItemListener {
        @Override
        public void onLoaded() {
            logger.info("Starting Settings Migration Process");

            ItemConfigMigrator migrator = new ItemConfigMigrator();

            for (Item item : Jenkins.getInstance().getAllItems()) {
                if (!migrator.migrate(item)) {
                    logger.info(String.format("Skipping job \"%s\" with type %s", item.getName(),
                            item.getClass().getName()));
                    continue;
                }
            }

            logger.info("Completed Settings Migration Process");
        }
    }
}
