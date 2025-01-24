package jenkins.plugins.slack.pipeline;

import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.ListBoxModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.CredentialsObtainer;
import jenkins.plugins.slack.HttpClient;
import jenkins.plugins.slack.Messages;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.user.SlackUserIdResolver;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
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
 * Pipeline step to resolve a Slack UserIds from the set of commit authors
 */
public class SlackUserIdsFromCommittersStep extends Step {

    private static final Logger logger = Logger.getLogger(SlackUserIdsFromCommittersStep.class.getName());

    private String tokenCredentialId;
    private boolean botUser;

    @DataBoundConstructor
    public SlackUserIdsFromCommittersStep() {
    }

    public String getTokenCredentialId() {
        return tokenCredentialId;
    }

    @DataBoundSetter
    public void setTokenCredentialId(final String tokenCredentialId) {
        this.tokenCredentialId = Util.fixEmpty(tokenCredentialId);
    }

    public boolean getBotUser() {
        return botUser;
    }

    @DataBoundSetter
    public void setBotUser(final boolean botUser) {
        this.botUser = botUser;
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new SlackUserIdsFromCommittersStepExcecution(this, context);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "slackUserIdsFromCommitters";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.slackUserIdsFromCommittersDisplayName();
        }

        public ListBoxModel doFillTokenCredentialIdItems(@AncestorInPath final Item item) {
            return findTokenCredentialIdItems(item);
        }
    }

    public static class SlackUserIdsFromCommittersStepExcecution extends SynchronousNonBlockingStepExecution<List<String>> {

        private static final long serialVersionUID = 1L;

        private transient final SlackUserIdsFromCommittersStep step;

        SlackUserIdsFromCommittersStepExcecution(final SlackUserIdsFromCommittersStep step, final StepContext context) {
            super(context);
            this.step = step;
        }

        @Override
        protected List<String> run() throws Exception {

            final Jenkins jenkins = Jenkins.get();
            final Item item = getItemForCredentials(getContext());
            final SlackNotifier.DescriptorImpl slackDesc = jenkins
                    .getDescriptorByType(SlackNotifier.DescriptorImpl.class);

            final String tokenCredentialId = step.tokenCredentialId != null ? step.tokenCredentialId
                    : slackDesc.getTokenCredentialId();
            final boolean botUser = step.botUser || slackDesc.isBotUser();
            final SlackUserIdResolver userIdResolver = slackDesc.getSlackUserIdResolver();

            final Run run = getContext().get(Run.class);
            final TaskListener listener = getContext().get(TaskListener.class);
            Objects.requireNonNull(listener, "Listener is mandatory here");

            if (!botUser) {
                listener.getLogger().println("The slackUserIdsFromCommitters step requires bot user mode");
                return null;
            }

            final String populatedToken;
            try {
                populatedToken = CredentialsObtainer.getTokenToUse(tokenCredentialId, item, null);
            } catch (final IllegalArgumentException e) {
                listener.error(Messages.notificationFailedWithException(e));
                return null;
            }

            final List<String> slackUserIds = new ArrayList<>();
            try (CloseableHttpClient client = getHttpClient()) {
                // include committer userIds in roomIds
                if (userIdResolver != null && run != null) {
                    userIdResolver.setAuthToken(populatedToken);
                    userIdResolver.setHttpClient(client);
                    final List<String> userIds = userIdResolver.resolveUserIdsForRun(run);
                    slackUserIds
                            .addAll(userIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList()));
                }
            } catch (final IOException e) {
                logger.log(Level.WARNING, "Error closing HttpClient", e);
            }
            return slackUserIds;
        }

        protected CloseableHttpClient getHttpClient() {
            final Jenkins jenkins = Jenkins.getInstanceOrNull();
            final ProxyConfiguration proxy = jenkins != null ? jenkins.proxy : null;
            return HttpClient.getCloseableHttpClient(proxy);
        }

    }
}
