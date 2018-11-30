package jenkins.plugins.slack.webhook;


import hudson.model.AbstractBuild;
import hudson.model.Project;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.webhook.model.SlackPostData;
import jenkins.plugins.slack.webhook.model.SlackTextMessage;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;




public class GetProjectLogCommand extends SlackRouterCommand implements RouterCommand<SlackTextMessage> {

    public GetProjectLogCommand(SlackPostData data) { 
        super(data);
    }

    @Override
    public SlackTextMessage execute(String... args) {
        String projectName = args[0];
        String buildNumber = args[1];

        SecurityContext ctx = ACL.impersonate(ACL.SYSTEM);

        List<String> log;

        try {
            Project project =
                Jenkins.getActiveInstance().getItemByFullName(projectName, Project.class);

            if (project == null)
                return new SlackTextMessage("Could not find project ("+projectName+")\n");

            AbstractBuild build =
                project.getBuildByNumber(Integer.parseInt(buildNumber));

            if (build == null)
                return new SlackTextMessage("Could not find build #"+buildNumber+" for ("+projectName+")\n");

            log = build.getLog(25);

        } catch (IOException ex) {
            return new SlackTextMessage("Error occured returning log: "+ex.getMessage());
        } finally {
            SecurityContextHolder.setContext(ctx);
        }

        StringBuilder builder = new StringBuilder("*" + projectName + "* *#" + buildNumber + "*\n```");
        for (String line : log) {
            builder.append(line).append("\n");
        }
        builder.append("```");

        return new SlackTextMessage(builder.toString());
    }
}
