package jenkins.plugins.slack.webhook;

import hudson.model.Project;
import hudson.security.ACL;
import hudson.security.ACLContext;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.webhook.model.SlackPostData;
import jenkins.plugins.slack.webhook.model.SlackTextMessage;
import jenkins.plugins.slack.webhook.model.SlackWebhookCause;

public class ScheduleJobCommand extends SlackRouterCommand implements RouterCommand<SlackTextMessage> {

    public ScheduleJobCommand(SlackPostData data) {
        super(data);
    }

    @Override
    public SlackTextMessage execute(String... args) {
        String projectName = args[0];

        try (ACLContext ignored = ACL.as(ACL.SYSTEM)) {
            Jenkins jenkins = Jenkins.getActiveInstance();

            Project project =
                    jenkins.getItemByFullName(projectName, Project.class);

            if (project == null) {
                return new SlackTextMessage("Could not find project (" + projectName + ")\n");
            }

            if (project.scheduleBuild(new SlackWebhookCause(this.getData().getUser_name()))) {
                return new SlackTextMessage("Build scheduled for project " + projectName + "\n");
            } else {
                return new SlackTextMessage("Build not scheduled due to an issue with Jenkins");
            }
        }
    }
}
