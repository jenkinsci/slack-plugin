package jenkins.plugins.slack.webhook;


import jenkins.model.Jenkins;

import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import hudson.security.ACL;

import java.util.List;

import jenkins.plugins.slack.webhook.model.SlackPostData;
import jenkins.plugins.slack.webhook.model.SlackTextMessage;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;




public class ListProjectsCommand extends SlackRouterCommand implements RouterCommand<SlackTextMessage> {

    public ListProjectsCommand(SlackPostData data) { 
        super(data);
    }

    @Override
    public SlackTextMessage execute(String... args) {

        SecurityContext ctx = ACL.impersonate(ACL.SYSTEM);

        String response = "*Projects:*\n";

        List<AbstractProject> jobs =
            Jenkins.getInstance().getAllItems(AbstractProject.class);

        SecurityContextHolder.setContext(ctx);

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
                    response += ">*"+job.getDisplayName() + "*\n>*Last Build:* #"+buildNumber+"\n>*Status:* "+status;
                    response += "\n\n\n";
                } else {
                    response += ">*"+job.getDisplayName() + "* :: *Last Build:* #"+buildNumber+" :: *Status:* "+status;
                    response += "\n\n";
                }
            }
        }

        if (jobs == null || jobs.size() == 0)
            response += ">_No projects found_";

        return new SlackTextMessage(response);
    }
}

