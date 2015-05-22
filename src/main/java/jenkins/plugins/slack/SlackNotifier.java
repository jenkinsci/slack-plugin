package jenkins.plugins.slack;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.JobPropertyDescriptor;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import jenkins.model.JenkinsLocationConfiguration;

public class SlackNotifier extends Notifier {

    private static final Logger logger = Logger.getLogger(SlackNotifier.class.getName());

    private String teamDomain;
    private String authToken;
    private String buildServerUrl;
    private String room;
    private String sendAs;

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

    public String getBuildServerUrl() {
        if(buildServerUrl == null || buildServerUrl == "") {
            JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();
            return jenkinsConfig.getUrl();
        }
        else {
            return buildServerUrl;
        }
    }

    public String getSendAs() {
        return sendAs;
    }

    @DataBoundConstructor
    public SlackNotifier(final String teamDomain, final String authToken, final String room, String buildServerUrl, final String sendAs) {
        super();
        this.teamDomain = teamDomain;
        this.authToken = authToken;
        this.buildServerUrl = buildServerUrl;
        this.room = room;
        this.sendAs = sendAs;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public SlackService newSlackService(String teamDomain, String token, String projectRoom) {
        // Settings are passed here from the job, if they are null, use global settings
        if (teamDomain == null) {
            teamDomain = getTeamDomain();
        }
        if (token == null) {
            token = getAuthToken();
        }
        if (projectRoom == null) {
            projectRoom = getRoom();
        }

        return new StandardSlackService(teamDomain, token, projectRoom);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return true;
    }

    public void update() {
        this.teamDomain = getDescriptor().teamDomain;
        this.authToken = getDescriptor().token;
        this.buildServerUrl = getDescriptor().buildServerUrl;
        this.room = getDescriptor().room;
        this.sendAs = getDescriptor().sendAs;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private String teamDomain;
        private String token;
        private String room;
        private String buildServerUrl;
        private String sendAs;

        public DescriptorImpl() {
            load();
        }

        public String getTeamDomain() {
            return teamDomain;
        }

        public String getToken() {
            return token;
        }

        public String getRoom() {
            return room;
        }

        public String getBuildServerUrl() {
            if(buildServerUrl == null || buildServerUrl == "") {
                JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();
                return jenkinsConfig.getUrl();
            }
            else {
                return buildServerUrl;
            }
        }

        public String getSendAs() {
            return sendAs;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public SlackNotifier newInstance(StaplerRequest sr) {
            if (teamDomain == null) {
                teamDomain = sr.getParameter("slackTeamDomain");
            }
            if (token == null) {
                token = sr.getParameter("slackToken");
            }
            if (buildServerUrl == null) {
                buildServerUrl = sr.getParameter("slackBuildServerUrl");
            }
            if (room == null) {
                room = sr.getParameter("slackRoom");
            }
            if (sendAs == null) {
                sendAs = sr.getParameter("slackSendAs");
            }
            return new SlackNotifier(teamDomain, token, room, buildServerUrl, sendAs);
        }

        @Override
        public boolean configure(StaplerRequest sr, JSONObject formData) throws FormException {
            teamDomain = sr.getParameter("slackTeamDomain");
            token = sr.getParameter("slackToken");
            room = sr.getParameter("slackRoom");
            buildServerUrl = sr.getParameter("slackBuildServerUrl");
            sendAs = sr.getParameter("slackSendAs");
            if(buildServerUrl == null || buildServerUrl == "") {
                JenkinsLocationConfiguration jenkinsConfig = new JenkinsLocationConfiguration();
                buildServerUrl = jenkinsConfig.getUrl();
            }
            if (buildServerUrl != null && !buildServerUrl.endsWith("/")) {
                buildServerUrl = buildServerUrl + "/";
            }
            save();
            return super.configure(sr, formData);
        }

        SlackService getSlackService(final String teamDomain, final String authToken, final String room) {
            return new StandardSlackService(teamDomain, authToken, room);
        }

        @Override
        public String getDisplayName() {
            return "Slack Notifications";
        }

        public FormValidation doTestConnection(@QueryParameter("slackTeamDomain") final String teamDomain,
                                               @QueryParameter("slackToken") final String authToken,
                                               @QueryParameter("slackRoom") final String room,
                                               @QueryParameter("slackBuildServerUrl") final String buildServerUrl) throws FormException {
            try {
                SlackService testSlackService = getSlackService(teamDomain, authToken, room);
                String message = "Slack/Jenkins plugin: you're all set on " + buildServerUrl;
                boolean success = testSlackService.publish(message, "good");
                return success ? FormValidation.ok("Success") : FormValidation.error("Failure");
            } catch (Exception e) {
                return FormValidation.error("Client error : " + e.getMessage());
            }
        }
    }

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
            if (startNotification) {
                Map<Descriptor<Publisher>, Publisher> map = build.getProject().getPublishersList().toMap();
                for (Publisher publisher : map.values()) {
                    if (publisher instanceof SlackNotifier) {
                        logger.info("Invoking Started...");
                        ((SlackNotifier) publisher).update();
                        new ActiveNotifier((SlackNotifier) publisher, listener).started(build);
                    }
                }
            }
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

        @Extension
        public static final class DescriptorImpl extends JobPropertyDescriptor {

            public String getDisplayName() {
                return "Slack Notifications";
            }

            @Override
            public boolean isApplicable(Class<? extends Job> jobType) {
                return true;
            }

            @Override
            public SlackJobProperty newInstance(StaplerRequest sr, JSONObject formData) throws hudson.model.Descriptor.FormException {
                return new SlackJobProperty(
                        sr.getParameter("slackTeamDomain"),
                        sr.getParameter("slackToken"),
                        sr.getParameter("slackProjectRoom"),
                        sr.getParameter("slackStartNotification") != null,
                        sr.getParameter("slackNotifyAborted") != null,
                        sr.getParameter("slackNotifyFailure") != null,
                        sr.getParameter("slackNotifyNotBuilt") != null,
                        sr.getParameter("slackNotifySuccess") != null,
                        sr.getParameter("slackNotifyUnstable") != null,
                        sr.getParameter("slackNotifyBackToNormal") != null,
                        sr.getParameter("slackNotifyRepeatedFailure") != null,
                        sr.getParameter("includeTestSummary") != null,
                        sr.getParameter("slackShowCommitList") != null,
                        sr.getParameter("includeCustomMessage") != null,
                        sr.getParameter("customMessage"));
            }

            public FormValidation doTestConnection(@QueryParameter("slackTeamDomain") final String teamDomain,
                                                   @QueryParameter("slackToken") final String authToken,
                                                   @QueryParameter("slackProjectRoom") final String room) throws FormException {
                try {
                    SlackService testSlackService = new StandardSlackService(teamDomain, authToken, room);
                    String message = "Slack/Jenkins plugin: you're all set.";
                    boolean success = testSlackService.publish(message, "good");
                    return success ? FormValidation.ok("Success") : FormValidation.error("Failure");
                } catch (Exception e) {
                    return FormValidation.error("Client error : " + e.getMessage());
                }
            }
        }
    }
}
