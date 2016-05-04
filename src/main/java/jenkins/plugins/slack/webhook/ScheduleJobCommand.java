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




public class ScheduleJobCommand extends SlackRouterCommand implements RouterCommand<SlackTextMessage> {

    public ScheduleJobCommand(SlackPostData data) { 
        super(data);
    }

    @Override
    public SlackTextMessage execute(String... args) {

        String projectName = args[0];
        SecurityContext ctx = ACL.impersonate(ACL.SYSTEM);

        String response = "";

        Project project =
            Jenkins.getInstance().getItemByFullName(projectName, Project.class);

        try {
            if (project == null)
                return new SlackTextMessage("Could not find project ("+projectName+")\n");

            if (project.scheduleBuild(new SlackWebhookCause(this.getData().getUser_name()))) {
                return new SlackTextMessage("Build scheduled for project "+ projectName+"\n");
            } else {
                return new SlackTextMessage("Build not scheduled due to an issue with Jenkins");
            }
        } finally {
            SecurityContextHolder.setContext(ctx);
        }
    }
}
