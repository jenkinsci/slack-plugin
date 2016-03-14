package jenkins.plugins.slack.webhook;


import jenkins.model.Jenkins;

import hudson.model.Build;
import hudson.model.Result;
import hudson.model.Project;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import hudson.security.ACL;

import java.io.IOException;

import java.util.List;
import java.util.ArrayList;

import org.kohsuke.stapler.interceptor.RequirePOST;

import jenkins.plugins.slack.webhook.model.SlackPostData;
import jenkins.plugins.slack.webhook.model.SlackTextMessage;
import jenkins.plugins.slack.webhook.model.SlackWebhookCause;

import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;




public class GetProjectLogCommand extends SlackRouterCommand implements RouterCommand<SlackTextMessage> {

    public GetProjectLogCommand(SlackPostData data) { 
        super(data);
    }

    @Override
    public SlackTextMessage execute(String... args) {
        String projectName = args[0];
        String buildNumber = args[1];

        SecurityContext ctx = ACL.impersonate(ACL.SYSTEM);

        List<String> log = new ArrayList<String>();

        try {
            Project project =
                Jenkins.getInstance().getItemByFullName(projectName, Project.class);

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

        String response = "*"+projectName+"* *#"+buildNumber+"*\n";
        response += "```";
        for (String line : log) {
            response += line + "\n";
        }
        response += "```";

        return new SlackTextMessage(response);
    }
}
