package jenkins.plugins.slack.webhook;


import hudson.security.ACLContext;
import jenkins.model.Jenkins;

import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import hudson.security.ACL;

import java.util.List;

import jenkins.plugins.slack.webhook.model.SlackPostData;
import jenkins.plugins.slack.webhook.model.SlackTextMessage;

public class ListProjectsCommand extends SlackRouterCommand implements RouterCommand<SlackTextMessage> {

    public ListProjectsCommand(SlackPostData data) { 
        super(data);
    }

    @Override
    public SlackTextMessage execute(String... args) {
        List<AbstractProject> jobs;
        try (ACLContext ignored = ACL.as(ACL.SYSTEM)) {
            Jenkins jenkins = Jenkins.getActiveInstance();

            jobs = jenkins.getAllItems(AbstractProject.class);
        }

        StringBuilder builder = new StringBuilder("*Projects:*\n");
        for (AbstractProject job : jobs) {
            if (job.isBuildable()) {
                AbstractBuild lastBuild = job.getLastBuild();
                String buildNumber = "TBD";
                String status = "TBD";
                if (lastBuild != null) {

                    buildNumber = Integer.toString(lastBuild.getNumber());

                    if (lastBuild.isBuilding()) {
                        status = "BUILDING";
                    }

                    Result result = lastBuild.getResult();

                    if (result != null) {
                        status = result.toString();
                    }
                }

                if (jobs.size() <= 10) {
                    builder.append(">*")
                        .append(job.getDisplayName())
                        .append("*\n>*Last Build:* #")
                        .append(buildNumber)
                        .append("\n>*Status:* ")
                        .append(status)
                        .append("\n\n\n");
                } else {
                    builder.append(">*")
                        .append(job.getDisplayName())
                        .append("* :: *Last Build:* #")
                        .append(buildNumber)
                        .append(" :: *Status:* ")
                        .append(status)
                        .append("\n\n");
                }
            }
        }
        if (jobs.size() == 0)
            builder.append(">_No projects found_");

        return new SlackTextMessage(builder.toString());
    }
}

