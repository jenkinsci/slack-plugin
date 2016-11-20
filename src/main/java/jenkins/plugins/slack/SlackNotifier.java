package jenkins.plugins.slack;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.HostnameRequirement;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.listeners.ItemListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

public class SlackNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(SlackNotifier.class.getName());

    private String teamDomain;
    private String authToken;
    private String authTokenCredentialId;
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
    private CommitInfoChoice commitInfoChoice;
    private boolean includeCustomMessage;
    private String customMessage;

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    public String getTeamDomain() {
        return teamDomain;
    }

    public String getRoom() {
        return room;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getAuthTokenCredentialId() {
        return authTokenCredentialId;
    }

    public String getSendAs() {
        return sendAs;
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

    public boolean getNotifyRepeatedFailure() {
        return notifyRepeatedFailure;
    }

    public boolean includeCustomMessage() {
        return includeCustomMessage;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public SlackNotifier(){}

    @DataBoundConstructor
    public SlackNotifier(final String teamDomain, final String authToken, final String room, final String authTokenCredentialId,
                         final String sendAs, final boolean startNotification, final boolean notifyAborted, final boolean notifyFailure,
                         final boolean notifyNotBuilt, final boolean notifySuccess, final boolean notifyUnstable, final boolean notifyBackToNormal,
                         final boolean notifyRepeatedFailure, final boolean includeTestSummary, CommitInfoChoice commitInfoChoice,
                         boolean includeCustomMessage, String customMessage) {
        super();
        this.teamDomain = teamDomain;
        this.authToken = authToken;
        this.authTokenCredentialId = StringUtils.trim(authTokenCredentialId);
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
        String authToken = this.authToken;
        if (StringUtils.isEmpty(authToken)) {
            authToken = getDescriptor().getToken();
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

        return new StandardSlackService(teamDomain, authToken, authTokenCredentialId, room);
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

        public String getRoom() {
            return room;
        }

        public String getSendAs() {
            return sendAs;
        }

        @SuppressWarnings("unused")
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
            CommitInfoChoice commitInfoChoice = CommitInfoChoice.forDisplayName(sr.getParameter("slackCommitInfoChoice"));
            boolean includeCustomMessage = "on".equals(sr.getParameter("includeCustomMessage"));
            String customMessage = sr.getParameter("customMessage");
            return new SlackNotifier(teamDomain, token, room, tokenCredentialId, sendAs, startNotification, notifyAborted,
                    notifyFailure, notifyNotBuilt, notifySuccess, notifyUnstable, notifyBackToNormal, notifyRepeatedFailure,
                    includeTestSummary, commitInfoChoice, includeCustomMessage, customMessage);
        }

        @Override
        public boolean configure(StaplerRequest sr, JSONObject formData) throws FormException {
            teamDomain = sr.getParameter("slackTeamDomain");
            token = sr.getParameter("slackToken");
            tokenCredentialId = formData.getJSONObject("slack").getString("tokenCredentialId");
            room = sr.getParameter("slackRoom");
            sendAs = sr.getParameter("slackSendAs");
            save();
            return super.configure(sr, formData);
        }

        SlackService getSlackService(final String teamDomain, final String authToken, final String authTokenCredentialId, final String room) {
            return new StandardSlackService(teamDomain, authToken, authTokenCredentialId, room);
        }

        @Override
        public String getDisplayName() {
            return "Slack Notifications";
        }

        public FormValidation doTestConnection(@QueryParameter("slackTeamDomain") final String teamDomain,
                                               @QueryParameter("slackToken") final String authToken,
                                               @QueryParameter("tokenCredentialId") final String authTokenCredentialId,
                                               @QueryParameter("slackRoom") final String room) throws FormException {
            try {
                String targetDomain = teamDomain;
                if (StringUtils.isEmpty(targetDomain)) {
                    targetDomain = this.teamDomain;
                }
                String targetToken = authToken;
                if (StringUtils.isEmpty(targetToken)) {
                    targetToken = this.token;
                }
                String targetTokenCredentialId = authTokenCredentialId;
                if (StringUtils.isEmpty(targetTokenCredentialId)) {
                    targetTokenCredentialId = this.tokenCredentialId;
                }
                String targetRoom = room;
                if (StringUtils.isEmpty(targetRoom)) {
                    targetRoom = this.room;
                }
                SlackService testSlackService = getSlackService(targetDomain, targetToken, targetTokenCredentialId, targetRoom);
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
        @SuppressWarnings("deprecation")
        @Override
        public void onLoaded() {
            logger.info("Starting Settings Migration Process");
            for (AbstractProject<?, ?> p : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
                logger.info("processing Job: " + p.getName());

                final SlackJobProperty slackJobProperty = p.getProperty(SlackJobProperty.class);

                if (slackJobProperty == null) {
                    logger.info(String
                            .format("Configuration is already up to date for \"%s\", skipping migration",
                                    p.getName()));
                    continue;
                }

                SlackNotifier slackNotifier = p.getPublishersList().get(SlackNotifier.class);
                
                if (slackNotifier == null) {
                    logger.info(String
                            .format("Configuration does not have a notifier for \"%s\", not migrating settings",
                                    p.getName()));
                } else {

                    //map settings
                    if (StringUtils.isBlank(slackNotifier.teamDomain)) {
                        slackNotifier.teamDomain = slackJobProperty.getTeamDomain();
                    }
                    if (StringUtils.isBlank(slackNotifier.authToken)) {
                        slackNotifier.authToken = slackJobProperty.getToken();
                    }
                    if (StringUtils.isBlank(slackNotifier.room)) {
                        slackNotifier.room = slackJobProperty.getRoom();
                    }
                    
                    slackNotifier.startNotification = slackJobProperty.getStartNotification();
    
                    slackNotifier.notifyAborted = slackJobProperty.getNotifyAborted();
                    slackNotifier.notifyFailure = slackJobProperty.getNotifyFailure();
                    slackNotifier.notifyNotBuilt = slackJobProperty.getNotifyNotBuilt();
                    slackNotifier.notifySuccess = slackJobProperty.getNotifySuccess();
                    slackNotifier.notifyUnstable = slackJobProperty.getNotifyUnstable();
                    slackNotifier.notifyBackToNormal = slackJobProperty.getNotifyBackToNormal();
                    slackNotifier.notifyRepeatedFailure = slackJobProperty.getNotifyRepeatedFailure();
    
                    slackNotifier.includeTestSummary = slackJobProperty.includeTestSummary();
                    slackNotifier.commitInfoChoice = slackJobProperty.getShowCommitList() ? CommitInfoChoice.AUTHORS_AND_TITLES : CommitInfoChoice.NONE;
                    slackNotifier.includeCustomMessage = slackJobProperty.includeCustomMessage();
                    slackNotifier.customMessage = slackJobProperty.getCustomMessage();
                }

                try {
                    //property section is not used anymore - remove
                    p.removeProperty(SlackJobProperty.class);
                    p.save();
                    logger.info("Configuration updated successfully");
                } catch (IOException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }
    }
}
