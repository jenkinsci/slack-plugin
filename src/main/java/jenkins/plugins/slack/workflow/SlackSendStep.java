package jenkins.plugins.slack.workflow;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import groovy.json.JsonOutput;
import hudson.AbortException;
import hudson.Extension;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.CredentialsObtainer;
import jenkins.plugins.slack.Messages;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.SlackRequest;
import jenkins.plugins.slack.SlackService;
import jenkins.plugins.slack.StandardSlackService;
import jenkins.plugins.slack.StandardSlackServiceBuilder;
import jenkins.plugins.slack.user.SlackUserIdResolver;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.groovy.JsonSlurper;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import static jenkins.plugins.slack.CredentialsObtainer.getItemForCredentials;
import static jenkins.plugins.slack.SlackNotifier.DescriptorImpl.findTokenCredentialIdItems;

/**
 * Workflow step to send a Slack channel notification.
 */
public class SlackSendStep extends Step {

    private static final Logger logger = Logger.getLogger(SlackSendStep.class.getName());

    private String message;
    private String timestamp;
    private String color;
    private String token;
    private String tokenCredentialId;
    private boolean botUser;
    private String channel;
    private String baseUrl;
    private String teamDomain;
    private boolean failOnError;
    private Object attachments;
    private Object blocks;
    private boolean replyBroadcast;
    private boolean sendAsText;
    private String iconEmoji;
    private String username;
    private boolean notifyCommitters;

    @NonNull
    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
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
    public void setAttachments(Object attachments) {
        this.attachments = attachments;
    }

    public Object getAttachments() {
        return attachments;
    }

    public Object getBlocks() {
        return blocks;
    }

    @DataBoundSetter
    public void setBlocks(Object blocks) {
        this.blocks = blocks;
    }

    @DataBoundSetter
    public void setMessage(String message) {
        this.message = Util.fixEmpty(message);
    }

    @DataBoundSetter
    public void setTimestamp(String timestamp) {
        this.timestamp = Util.fixEmpty(timestamp);
    }

    public boolean getReplyBroadcast() {
        return replyBroadcast;
    }

    @DataBoundSetter
    public void setReplyBroadcast(boolean replyBroadcast) {
        this.replyBroadcast = replyBroadcast;
    }

    public boolean getSendAsText() {
        return sendAsText;
    }

    @DataBoundSetter
    public void setSendAsText(boolean sendAsText) {
        this.sendAsText = sendAsText;
    }


    @DataBoundConstructor
    public SlackSendStep() {
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    @DataBoundSetter
    public void setIconEmoji(String iconEmoji) {
        this.iconEmoji = Util.fixEmpty(iconEmoji);
    }

    public String getUsername() {
        return username;
    }

    @DataBoundSetter
    public void setUsername(String username) {
        this.username = Util.fixEmpty(username);
    }

    public boolean getNotifyCommitters() {
        return notifyCommitters;
    }

    @DataBoundSetter
    public void setNotifyCommitters(boolean notifyCommitters) {
        this.notifyCommitters = notifyCommitters;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new SlackSendStepExecution(this, context);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "slackSend";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.slackSendStepDisplayName();
        }

        public ListBoxModel doFillTokenCredentialIdItems(@AncestorInPath Item item) {
            return findTokenCredentialIdItems(item);
        }
    }

    public static class SlackSendStepExecution extends SynchronousNonBlockingStepExecution<SlackResponse> {

        private static final long serialVersionUID = 1L;

        private transient final SlackSendStep step;

        SlackSendStepExecution(SlackSendStep step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected SlackResponse run() throws Exception {

            Jenkins jenkins = Jenkins.get();
            Item item = getItemForCredentials(getContext());
            SlackNotifier.DescriptorImpl slackDesc = jenkins.getDescriptorByType(SlackNotifier.DescriptorImpl.class);

            String baseUrl = step.baseUrl != null ? step.baseUrl : slackDesc.getBaseUrl();
            String teamDomain = step.teamDomain != null ? step.teamDomain : slackDesc.getTeamDomain();
            String tokenCredentialId = step.tokenCredentialId != null ? step.tokenCredentialId : slackDesc
                    .getTokenCredentialId();
            String token = step.token;
            boolean botUser = step.botUser || slackDesc.isBotUser();
            String channel = step.channel != null ? step.channel : slackDesc.getRoom();
            String color = step.color != null ? step.color : "";
            boolean sendAsText = step.sendAsText || slackDesc.isSendAsText();
            String iconEmoji = step.iconEmoji != null ? step.iconEmoji : slackDesc.getIconEmoji();
            String username = step.username != null ? step.username : slackDesc.getUsername();
            boolean notifyCommitters = step.notifyCommitters;
            SlackUserIdResolver userIdResolver = slackDesc.getSlackUserIdResolver();

            Run run = getContext().get(Run.class);
            TaskListener listener = getContext().get(TaskListener.class);
            Objects.requireNonNull(listener, "Listener is mandatory here");

            listener.getLogger().println(Messages.slackSendStepValues(
                    defaultIfEmpty(baseUrl), defaultIfEmpty(teamDomain), channel, defaultIfEmpty(color), botUser,
                    defaultIfEmpty(tokenCredentialId), notifyCommitters, defaultIfEmpty(iconEmoji), defaultIfEmpty(username), defaultIfEmpty(step.timestamp))
            );
            final String populatedToken;
            try {
                populatedToken = CredentialsObtainer.getTokenToUse(tokenCredentialId, item, token);
            } catch (IllegalArgumentException e) {
                listener.error(Messages
                        .notificationFailedWithException(e));
                return null;
            }

            SlackService slackService = getSlackService(
                run,
                baseUrl,
                teamDomain,
                botUser,
                channel,
                step.replyBroadcast,
                sendAsText,
                iconEmoji,
                username,
                populatedToken,
                notifyCommitters,
                userIdResolver
            );

            final boolean publishSuccess;
            if (sendAsText) {
                if (step.timestamp != null) {
                    publishSuccess = slackService.publish(step.message, new JSONArray(), color, step.timestamp);
                } else {
                    publishSuccess = slackService.publish(step.message, new JSONArray(), color);
                }
            } else if (step.attachments != null || step.blocks != null) {
                SlackRequest.SlackRequestBuilder requestBuilder = SlackRequest.builder()
                    .withMessage(step.message)
                    .withTimestamp(step.timestamp);

                if (step.attachments != null) {
                    JSONArray attachmentsJsonArray = getAttachmentsAsJSONArray();
                    for (Object object : attachmentsJsonArray) {
                        if (object instanceof JSONObject) {
                            JSONObject jsonNode = ((JSONObject) object);
                            if (!jsonNode.has("fallback")) {
                                jsonNode.put("fallback", step.message);
                            }
                        }
                    }

                    requestBuilder = requestBuilder.withAttachments(attachmentsJsonArray)
                        .withColor(color);
                }

                if (step.blocks != null) {
                    JSONArray blocksJsonArray = getBlocksAsJSONArray();
                    requestBuilder = requestBuilder.withBlocks(blocksJsonArray);
                }

                publishSuccess = slackService.publish(requestBuilder.build());
            } else if (step.message != null) {
                if (step.timestamp != null) {
                    publishSuccess = slackService.publish(step.message, color, step.timestamp);
                } else {
                    publishSuccess = slackService.publish(step.message, color);
                }
            } else {
                listener.error(Messages
                        .notificationFailedWithException(new IllegalArgumentException("No message, attachments or blocks provided")));
                return null;
            }
            SlackResponse response = null;
            String responseString = slackService.getResponseString();
            if (publishSuccess) {
                if (responseString != null) {
                    try {
                        org.json.JSONObject result = new org.json.JSONObject(responseString);
                        response = new SlackResponse(result, slackService);
                    } catch (org.json.JSONException ex) {
                        listener.error(Messages.failedToParseSlackResponse(responseString));
                        if (step.failOnError) {
                            throw ex;
                        }
                    }
                } else {
                    return new SlackResponse(slackService);
                }
            } else if (step.failOnError) {
                if (responseString != null) {
                    throw new AbortException(Messages.notificationFailedWithException(responseString));
                }
                throw new AbortException(Messages.notificationFailed());
            } else {
                if (responseString != null) {
                    listener.error(Messages.notificationFailedWithException(responseString));
                }
                listener.error(Messages.notificationFailed());
            }
            return response;
        }

        JSONArray getBlocksAsJSONArray() throws Exception {
            final TaskListener listener = getContext().get(TaskListener.class);
            final String jsonString = JsonOutput.toJson(step.blocks);

            return convertStringToJsonArray(listener, jsonString, "Blocks");
        }

        private JSONArray convertStringToJsonArray(TaskListener listener, String jsonString, String fieldType) {
            JsonSlurper jsonSlurper = new JsonSlurper();
            JSON json;
            try {
                json = jsonSlurper.parseText(jsonString);
            } catch (JSONException e) {
                listener.error(Messages.notificationFailedWithException(e));
                return null;
            }
            if (!(json instanceof JSONArray)) {
                listener.error(Messages.notificationFailedWithException(new IllegalArgumentException(fieldType + " must be JSONArray")));
                return null;
            }
            return (JSONArray) json;
        }

        JSONArray getAttachmentsAsJSONArray() throws Exception {
            final TaskListener listener = getContext().get(TaskListener.class);
            final String jsonString;
            if (step.attachments instanceof String) {
                jsonString = (String) step.attachments;
            } else {
                jsonString = JsonOutput.toJson(step.attachments);
            }

            return convertStringToJsonArray(listener, jsonString, "Attachments");
        }

        private String defaultIfEmpty(String value) {
            return Util.fixEmpty(value) != null ? value : Messages.slackSendStepValuesEmptyMessage();
        }

        //streamline unit testing
        SlackService getSlackService(Run run, String baseUrl, String team, boolean botUser, String channel, boolean replyBroadcast, boolean sendAsText, String iconEmoji, String username, String populatedToken, boolean notifyCommitters, SlackUserIdResolver userIdResolver) {
            return new StandardSlackService(
                    new StandardSlackServiceBuilder()
                        .withRun(run)
                        .withBaseUrl(baseUrl)
                        .withTeamDomain(team)
                        .withBotUser(botUser)
                        .withRoomId(channel)
                        .withReplyBroadcast(replyBroadcast)
                        .withIconEmoji(iconEmoji)
                        .withUsername(username)
                        .withPopulatedToken(populatedToken)
                        .withNotifyCommitters(notifyCommitters)
                        .withSlackUserIdResolver(userIdResolver)
                    );
        }
    }
}
