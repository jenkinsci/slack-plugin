package jenkins.plugins.slack.pipeline;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.CredentialsObtainer;
import jenkins.plugins.slack.Messages;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.cache.SlackChannelIdCache;
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

public class SlackUploadFileStep extends Step {

    private String credentialId;
    private String channel;
    private String initialComment;
    private String filePath;
    private boolean failOnError;

    @DataBoundConstructor
    public SlackUploadFileStep(String filePath) {
        this.filePath = filePath;
    }

    @DataBoundSetter
    public void setCredentialId(String credentialId) {
        this.credentialId = Util.fixEmpty(credentialId);
    }

    @DataBoundSetter
    public void setChannel(String channel) {
        this.channel = Util.fixEmpty(channel);
    }

    @DataBoundSetter
    public void setInitialComment(String initialComment) {
        this.initialComment = Util.fixEmpty(initialComment);
    }

    public String getCredentialId() {
        return credentialId;
    }

    public String getChannel() {
        return channel;
    }

    public String getInitialComment() {
        return initialComment;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    @DataBoundSetter
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public StepExecution start(StepContext context) {
        return new SlackUploadFileStepExecution(this, context);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class, FilePath.class);
        }

        @Override
        public String getFunctionName() {
            return "slackUploadFile";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.slackFileUploadDisplayName();
        }

        public ListBoxModel doFillCredentialIdItems(@AncestorInPath Item item) {
            return findTokenCredentialIdItems(item);
        }
    }

    public static class SlackUploadFileStepExecution extends SynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

        private transient final SlackUploadFileStep step;

        SlackUploadFileStepExecution(SlackUploadFileStep step, StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected Void run() throws IOException, InterruptedException, ExecutionException {
            TaskListener listener = getContext().get(TaskListener.class);
            FilePath filePath = getContext().get(FilePath.class);

            Item item = getItemForCredentials(getContext());
            SlackNotifier.DescriptorImpl slackDesc = Jenkins.get().getDescriptorByType(SlackNotifier.DescriptorImpl.class);

            String tokenCredentialId = step.credentialId != null ? step.credentialId : slackDesc
                    .getTokenCredentialId();

            String populatedToken = CredentialsObtainer.getTokenToUse(tokenCredentialId, item, null);
            String channel = step.channel != null ? step.channel : slackDesc.getRoom();

            String channelId;
            try {
                channelId = SlackChannelIdCache.getChannelId(populatedToken, cleanChannelName(channel));
                if (channelId == null) {
                    throw new AbortException("Failed uploading file to slack, channel not found: " + channel);
                }
            } catch (CompletionException | SlackChannelIdCache.HttpStatusCodeException e) {
                throw new AbortException("Failed uploading file to slack, channel not found: " + channel + ", error: " + e.getMessage());
            }

            String threadTs = getThreadTs(channel);

            SlackFileRequest slackFileRequest = new SlackFileRequest(
                    filePath, populatedToken, channelId, step.initialComment, step.filePath, threadTs
            );

            assert filePath != null;
            VirtualChannel virtualChannel = filePath.getChannel();
            assert virtualChannel != null;

            Boolean result = virtualChannel.callAsync(new SlackUploadFileRunner(listener, Jenkins.get().proxy, slackFileRequest)).get();
            if (!result) {
                String errorMessage = "Failed uploading file to slack";
                if (step.failOnError) {
                    throw new AbortException(errorMessage);
                } else {
                    listener.error(errorMessage);
                }
            }

            return null;
        }
    }

    private static String getThreadTs(String channelName) {
        String[] splitForThread = channelName.split(":", 2);
        if (splitForThread.length == 2) {
            return splitForThread[1];
        }
        return null;
    }

    private static String cleanChannelName(String channelName) {
        String[] splitForThread = channelName.split(":", 2);
        String channel = channelName;
        if (splitForThread.length == 2) {
            channel = splitForThread[0];
        }
        if (channel.startsWith("#")) {
            return channel.substring(1);
        }
        return channelName;
    }
}
