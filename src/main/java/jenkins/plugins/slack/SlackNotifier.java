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
import hudson.model.listeners.ItemListener;
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
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

public class SlackNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(SlackNotifier.class.getName());

    private String baseUrl;
    private String teamDomain;
    private String authToken;
    private String tokenCredentialId;
    private boolean botUser;
    private String room;
    private String sendAs;
    private boolean startNotification;
    private boolean notifySuccess;
    private boolean notifyAborted;
    private boolean notifyNotBuilt;
    private boolean notifyUnstable;
    private boolean notifyRegression;
    private boolean notifyFailure;
    private boolean notifyBackToNormal;
    private boolean notifyRepeatedFailure;
    private boolean includeTestSummary;
    private boolean includeFailedTests;
    private CommitInfoChoice commitInfoChoice;
    private boolean includeCustomMessage;
    private String customMessage;

    /** @deprecated use {@link #tokenCredentialId} */
    private transient String authTokenCredentialId;

    public String getAuthTokenCredentialId() {
        return tokenCredentialId;
    }

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

    public boolean getNotifyRegression() {
        return notifyRegression;
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

    @DataBoundConstructor
    public SlackNotifier(final String baseUrl, final String teamDomain, final String authToken, final boolean botUser, final String room, final String tokenCredentialId,
                         final String sendAs, final boolean startNotification, final boolean notifyAborted, final boolean notifyFailure,
                         final boolean notifyNotBuilt, final boolean notifySuccess, final boolean notifyUnstable, final boolean notifyRegression, final boolean notifyBackToNormal,
                         final boolean notifyRepeatedFailure, final boolean includeTestSummary, final boolean includeFailedTests,
                         CommitInfoChoice commitInfoChoice, boolean includeCustomMessage, String customMessage) {
        super();
        this.baseUrl = baseUrl;
        if(this.baseUrl != null && !this.baseUrl.isEmpty() && !this.baseUrl.endsWith("/")) {
            this.baseUrl += "/";
        }
        this.teamDomain = teamDomain;
        this.authToken = authToken;
        this.tokenCredentialId = StringUtils.trim(tokenCredentialId);
        this.botUser = botUser;
        this.room = room;
        this.sendAs = sendAs;
        this.startNotification = startNotification;
        this.notifyAborted = notifyAborted;
        this.notifyFailure = notifyFailure;
        this.notifyNotBuilt = notifyNotBuilt;
        this.notifySuccess = notifySuccess;
        this.notifyUnstable = notifyUnstable;
        this.notifyRegression = notifyRegression;
        this.notifyBackToNormal = notifyBackToNormal;
        this.notifyRepeatedFailure = notifyRepeatedFailure;
        this.includeTestSummary = includeTestSummary;
        this.includeFailedTests = includeFailedTests;
        this.commitInfoChoice = commitInfoChoice;
        this.includeCustomMessage = includeCustomMessage;
        this.customMessage = customMessage;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public SlackService newSlackService(AbstractBuild r, BuildListener listener) {
        String teamDomain = this.teamDomain;
        if (StringUtils.isEmpty(teamDomain)) {
            teamDomain = getDescriptor().getTeamDomain();
        }

        String baseUrl = this.baseUrl;
        if (StringUtils.isEmpty(baseUrl)) {
            baseUrl = getDescriptor().getBaseUrl();
        }

        String authToken = this.authToken;
        boolean botUser = this.botUser;
        if (StringUtils.isEmpty(authToken)) {
            authToken = getDescriptor().getToken();
            botUser = getDescriptor().isBotUser();
        }
        String authTokenCredentialId = this.tokenCredentialId;
        if (StringUtils.isEmpty(authTokenCredentialId)) {
            authTokenCredentialId = getDescriptor().getTokenCredentialId();
        }
        String room = this.room;
        if (StringUtils.isEmpty(room)) {
            room = getDescriptor().getRoom();
        }

        EnvVars env;
        try {
            env = r.getEnvironment(listener);
        } catch (Exception e) {
            listener.getLogger().println("Error retrieving environment vars: " + e.getMessage());
            env = new EnvVars();
        }
        baseUrl = env.expand(baseUrl);
        teamDomain = env.expand(teamDomain);
        authToken = env.expand(authToken);
        authTokenCredentialId = env.expand(authTokenCredentialId);
        room = env.expand(room);

        return new StandardSlackService(baseUrl, teamDomain, authToken, authTokenCredentialId, botUser, room);
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return notifyRegression;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        logger.info("Performing complete notifications");
        new ActiveNotifier((SlackNotifier) this, listener).completed(build);
        if (notifyRegression) {
            logger.info("Performing finalize notifications");
            new ActiveNotifier((SlackNotifier) this, listener).finalized(build);
        }
        return true;
    }

    @Override
    public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
        if (startNotification) {
            logger.info("Performing start notifications");
            new ActiveNotifier((SlackNotifier) this, listener).started(build);
        }
        return super.prebuild(build, listener);
    }

    @Extension @Symbol("slackNotifier")
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String baseUrl;
        private String teamDomain;
        private String token;
        private String tokenCredentialId;
        private boolean botUser;
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

        public String getToken() {
            return token;
        }

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

        @DataBoundSetter
        public void setBotUser(boolean botUser) {
            this.botUser = botUser;
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

            if(context == null && !Jenkins.getInstance().hasPermission(Jenkins.ADMINISTER) ||
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

        //WARN users that they should not use the plain/exposed token, but rather the token credential id
        public FormValidation doCheckToken(@QueryParameter String value) {
            //always show the warning - TODO investigate if there is a better way to handle this
            return FormValidation.warning("Exposing your Integration Token is a security risk. Please use the Integration Token Credential ID");
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

        SlackService getSlackService(final String baseUrl, final String teamDomain, final String authToken, final String authTokenCredentialId, final boolean botUser, final String room) {
            return new StandardSlackService(baseUrl, teamDomain, authToken, authTokenCredentialId, botUser, room);
        }

        @Override
        public String getDisplayName() {
            return "Slack Notifications";
        }

        public FormValidation doTestConnection(@QueryParameter("baseUrl") final String baseUrl,
                                               @QueryParameter("teamDomain") final String teamDomain,
                                               @QueryParameter("token") final String authToken,
                                               @QueryParameter("tokenCredentialId") final String tokenCredentialId,
                                               @QueryParameter("botUser") final boolean botUser,
                                               @QueryParameter("room") final String room) {

            try {
                String targetUrl = baseUrl;
                if(targetUrl != null && !targetUrl.isEmpty() && !targetUrl.endsWith("/")) {
                    targetUrl += "/";
                }

                // override with values from global config if fields aren't set
                if (StringUtils.isEmpty(targetUrl)) {
                    targetUrl = this.baseUrl;
                }
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
                String targetTokenCredentialId = tokenCredentialId;
                if (StringUtils.isEmpty(targetTokenCredentialId)) {
                    targetTokenCredentialId = this.tokenCredentialId;
                }
                String targetRoom = room;
                if (StringUtils.isEmpty(targetRoom)) {
                    targetRoom = this.room;
                }
                SlackService testSlackService = getSlackService(targetUrl, targetDomain, targetToken, targetTokenCredentialId, targetBotUser, targetRoom);
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
        private boolean notifyRegression;
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
                                boolean notifyRegression,
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
            this.notifyRegression = notifyRegression;
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
        public boolean getNotifyRegression() {
            return notifyRegression;
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

            List<Item> items = Jenkins.getInstance().getAllItems();


            if (null != items) {
                for (Item item : items) {
                    if (!migrator.migrate(item)) {
                        logger.info(String.format("Skipping job \"%s\" with type %s", item.getName(),
                                item.getClass().getName()));
                        continue;
                    }
                }
            }

            logger.info("Completed Settings Migration Process");
        }
    }
}
