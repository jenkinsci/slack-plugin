package jenkins.plugins.slack.workflow;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.HostnameRequirement;
import hudson.AbortException;
import hudson.Extension;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Project;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.Messages;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.SlackService;
import jenkins.plugins.slack.StandardSlackService;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.groovy.JsonSlurper;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

/**
 * Workflow step to send a Slack channel notification.
 */
public class SlackSendStep extends AbstractStepImpl {

    private String message;
    private String color;
    private String token;
    private String tokenCredentialId;
    private boolean botUser;
    private String channel;
    private String baseUrl;
    private String teamDomain;
    private boolean failOnError;
    private String attachments;
    private boolean replyBroadcast;


    @Nonnull
    public String getMessage() {
        return message;
    }

    public String getColor() {
        return color;
    }

    @DataBoundSetter
    public void setColor(String color) {
        this.color = Util.fixEmpty(color);
    }

    public String getToken() {
        return token;
    }

    @DataBoundSetter
    public void setToken(String token) {
        this.token = Util.fixEmpty(token);
    }

    public String getTokenCredentialId() {
        return tokenCredentialId;
    }

    @DataBoundSetter
    public void setTokenCredentialId(String tokenCredentialId) {
        this.tokenCredentialId = Util.fixEmpty(tokenCredentialId);
    }

    public boolean getBotUser() {
        return botUser;
    }

    @DataBoundSetter
    public void setBotUser(boolean botUser) {
        this.botUser = botUser;
    }

    public String getChannel() {
        return channel;
    }

    @DataBoundSetter
    public void setChannel(String channel) {
        this.channel = Util.fixEmpty(channel);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    @DataBoundSetter
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = Util.fixEmpty(baseUrl);
        if (this.baseUrl != null && !this.baseUrl.isEmpty() && !this.baseUrl.endsWith("/")) {
            this.baseUrl += "/";
        }
    }

    public String getTeamDomain() {
        return teamDomain;
    }

    @DataBoundSetter
    public void setTeamDomain(String teamDomain) {
        this.teamDomain = Util.fixEmpty(teamDomain);
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    @DataBoundSetter
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @DataBoundSetter
    public void setAttachments(String attachments) {
        this.attachments = attachments;
    }

    public String getAttachments() {
        return attachments;
    }

    @DataBoundSetter
    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getReplyBroadcast() {
        return replyBroadcast;
    }

    @DataBoundSetter
    public void setReplyBroadcast(boolean replyBroadcast) {
        this.replyBroadcast = replyBroadcast;
    }

    @DataBoundConstructor
    public SlackSendStep() {
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        public DescriptorImpl() {
            super(SlackSendStepExecution.class);
        }

        @Override
        public String getFunctionName() {
            return "slackSend";
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.SlackSendStepDisplayName();
        }

        public ListBoxModel doFillTokenCredentialIdItems(@AncestorInPath Project project) {

            Jenkins jenkins = Jenkins.getActiveInstance();

            if (project == null && !jenkins.hasPermission(Jenkins.ADMINISTER) ||
                    project != null && !project.hasPermission(Item.EXTENDED_READ)) {
                return new StandardListBoxModel();
            }

            return new StandardListBoxModel()
                    .withEmptySelection()
                    .withAll(lookupCredentials(
                            StringCredentials.class,
                            project,
                            ACL.SYSTEM,
                            new HostnameRequirement("*.slack.com"))
                    );
        }

        //WARN users that they should not use the plain/exposed token, but rather the token credential id
        public FormValidation doCheckToken(@QueryParameter String value) {
            //always show the warning - TODO investigate if there is a better way to handle this
            return FormValidation
                    .warning("Exposing your Integration Token is a security risk. Please use the Integration Token Credential ID");
        }
    }

    public static class SlackSendStepExecution extends AbstractSynchronousNonBlockingStepExecution<SlackResponse> {

        private static final long serialVersionUID = 1L;

        @Inject
        transient SlackSendStep step;

        @StepContextParameter
        transient TaskListener listener;

        @Override
        protected SlackResponse run() throws Exception {

            Jenkins jenkins = Jenkins.getActiveInstance();

            SlackNotifier.DescriptorImpl slackDesc = jenkins.getDescriptorByType(SlackNotifier.DescriptorImpl.class);

            String baseUrl = step.baseUrl != null ? step.baseUrl : slackDesc.getBaseUrl();
            String teamDomain = step.teamDomain != null ? step.teamDomain : slackDesc.getTeamDomain();
            String tokenCredentialId = step.tokenCredentialId != null ? step.tokenCredentialId : slackDesc
                    .getTokenCredentialId();
            String token = step.token != null ? step.token : slackDesc.getToken();
            boolean botUser = step.botUser || slackDesc.isBotUser();
            String channel = step.channel != null ? step.channel : slackDesc.getRoom();
            String color = step.color != null ? step.color : "";

            listener.getLogger().println(Messages.SlackSendStepValues(
                    defaultIfEmpty(baseUrl), defaultIfEmpty(teamDomain), channel, defaultIfEmpty(color), botUser,
                    defaultIfEmpty(tokenCredentialId))
            );

            SlackService slackService = getSlackService(
                    baseUrl, teamDomain, token, tokenCredentialId, botUser, channel, step.replyBroadcast
            );
            boolean publishSuccess;
            if (step.attachments != null) {
                JsonSlurper jsonSlurper = new JsonSlurper();
                JSON json;
                try {
                    json = jsonSlurper.parseText(step.attachments);
                } catch (JSONException e) {
                    listener.error(Messages.NotificationFailedWithException(e));
                    return null;
                }
                if (!(json instanceof JSONArray)) {
                    listener.error(Messages
                            .NotificationFailedWithException(new IllegalArgumentException("Attachments must be JSONArray")));
                    return null;
                }
                JSONArray jsonArray = (JSONArray) json;
                for (Object object : jsonArray) {
                    if (object instanceof JSONObject) {
                        JSONObject jsonNode = ((JSONObject) object);
                        if (!jsonNode.has("fallback")) {
                            jsonNode.put("fallback", step.message);
                        }
                    }
                }
                publishSuccess = slackService.publish(step.message, jsonArray, color);
            } else if (step.message != null) {
                publishSuccess = slackService.publish(step.message, color);
            } else {
                listener.error(Messages
                        .NotificationFailedWithException(new IllegalArgumentException("No message or attachments provided")));
                return null;
            }
            SlackResponse response = null;
            if (publishSuccess) {
                String responseString = slackService.getResponseString();
                if (responseString != null) {
                    try {
                        org.json.JSONObject result = new org.json.JSONObject(responseString);
                        response = new SlackResponse(result);
                    } catch (org.json.JSONException ex) {
                        listener.error(Messages.FailedToParseSlackResponse());
                        if (step.failOnError) {
                            throw ex;
                        }
                    }
                } else {
                    return new SlackResponse();
                }
            } else if (step.failOnError) {
                throw new AbortException(Messages.NotificationFailed());
            } else {
                listener.error(Messages.NotificationFailed());
            }
            return response;
        }

        private String defaultIfEmpty(String value) {
            return Util.fixEmpty(value) != null ? value : Messages.SlackSendStepValuesEmptyMessage();
        }

        //streamline unit testing
        SlackService getSlackService(String baseUrl, String team, String token, String tokenCredentialId, boolean botUser, String channel, boolean replyBroadcast) {
            return new StandardSlackService(baseUrl, team, token, tokenCredentialId, botUser, channel, replyBroadcast);
        }
    }
}
