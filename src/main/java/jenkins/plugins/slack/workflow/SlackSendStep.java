package jenkins.plugins.slack.workflow;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.SlackService;
import jenkins.plugins.slack.StandardSlackService;
import jenkins.plugins.slack.Messages;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Workflow step to send a Slack channel notification.
 */
public class SlackSendStep extends AbstractStepImpl {

	private static final Logger logger = Logger.getLogger(SlackSendStep.class.getName());

	public final String message;

	@DataBoundSetter
	public String color;

	@DataBoundSetter
	public String token;

	@DataBoundSetter
	public String channel;

	@DataBoundSetter
	public String teamDomain;

    @DataBoundSetter
    public boolean failOnError;

	@DataBoundConstructor
	public SlackSendStep(@Nonnull String message) {
		this.message = message;
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

		@Override
		public String getDisplayName() {
			return Messages.SlackSendStepDisplayName();
		}
	}

	public static class SlackSendStepExecution extends AbstractSynchronousNonBlockingStepExecution<Void> {

        private static final long serialVersionUID = 1L;

		@Inject
        private transient SlackSendStep step;

		@StepContextParameter
        private transient Run<?,?> run;
		@StepContextParameter private transient TaskListener listener;

		@Override
		protected Void run() throws Exception {

			//default to global config values if not set in step, but allow step to override all global settings
			SlackNotifier.DescriptorImpl slackDesc = Jenkins.getInstance().getDescriptorByType(SlackNotifier.DescriptorImpl.class);
			String team = step.teamDomain != null ? step.teamDomain : slackDesc.getTeamDomain();
			String token = step.token != null ? step.token : slackDesc.getToken();
			String channel = step.channel != null ? step.channel : slackDesc.getRoom();
			String color = step.color != null ? step.color : "";

			//placing in console log to simplify testing of retrieving values from global config or from step field; also used for tests
			listener.getLogger().println(Messages.SlackSendStepConfig(step.teamDomain == null, step.token == null, step.channel == null, step.color == null));

			SlackService testSlackService = new StandardSlackService(team, token, channel);
			boolean publishSuccess = testSlackService.publish(step.message, color);
            if(!publishSuccess && step.failOnError) {
                throw new AbortException(Messages.NotificationFailed());
            } else if(!publishSuccess) {
                listener.error(Messages.NotificationFailed());
            }
			return null;
		}

	}

}
