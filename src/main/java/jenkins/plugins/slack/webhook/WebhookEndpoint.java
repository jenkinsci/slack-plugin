package jenkins.plugins.slack.webhook;


import jenkins.model.Jenkins;
import jenkins.model.GlobalConfiguration;

import hudson.Extension;

import hudson.model.UnprotectedRootAction;

import javax.servlet.ServletException;

import java.io.IOException;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

import java.util.logging.Logger;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import org.kohsuke.stapler.interceptor.RequirePOST;

import jenkins.plugins.slack.webhook.model.JsonResponse;
import jenkins.plugins.slack.webhook.model.SlackPostData;
import jenkins.plugins.slack.webhook.model.SlackTextMessage;

import jenkins.plugins.slack.webhook.exception.CommandRouterException;
import jenkins.plugins.slack.webhook.exception.RouteNotFoundException;




@Extension
public class WebhookEndpoint implements UnprotectedRootAction {

    private GlobalConfig globalConfig;

    private static final Logger LOGGER =
        Logger.getLogger(WebhookEndpoint.class.getName());

    public WebhookEndpoint() {
        globalConfig = GlobalConfiguration.all().get(GlobalConfig.class);
    }

    @Override
    public String getUrlName() {
        String url = globalConfig.getSlackOutgoingWebhookURL();
        if (url == null || url.equals(""))
            return UUID.randomUUID().toString().replaceAll("-", "");

        return "/"+url;
    }

    @RequirePOST
    public HttpResponse doIndex(StaplerRequest req) throws IOException,
        ServletException {

        if (globalConfig.getSlackOutgoingWebhookToken() == null ||
            globalConfig.getSlackOutgoingWebhookToken().equals("")) {
            return new JsonResponse(new SlackTextMessage("Slack token not set"),
                StaplerResponse.SC_OK); 
        }

        SlackPostData data = new SlackPostData();
        req.bindParameters(data);

        if (!globalConfig.getSlackOutgoingWebhookToken().equals(data.getToken()))
            return new JsonResponse(new SlackTextMessage("Invalid Slack token"),
                StaplerResponse.SC_OK); 
    
        String commandText = data.getText();
        if (commandText == null || commandText.isEmpty())
            return new JsonResponse(new SlackTextMessage("Invalid command, text field required"),
                StaplerResponse.SC_OK);

        String triggerWord = data.getTrigger_word();
        if (triggerWord == null || triggerWord.isEmpty())
            return new JsonResponse(new SlackTextMessage("Invalid command, trigger_word field required"),
                StaplerResponse.SC_OK);

        if (!commandText.startsWith(triggerWord))
            return new JsonResponse(new SlackTextMessage("Invalid command, invalid trigger_word"),
                StaplerResponse.SC_OK);

        commandText = commandText.trim().replaceFirst(triggerWord, "").trim();

        CommandRouter<SlackTextMessage> router =
            new CommandRouter<SlackTextMessage>();

        try {
            router.addRoute("^list projects",
                triggerWord+" list projects",
                "Return a list of buildable projects",
                new ListProjectsCommand(data))
            .addRoute("^run ([\\p{L}\\p{N}\\p{ASCII}\\W]+)",
                triggerWord+" run <project_name>",
                "Schedule a run for <project_name>",
                new ScheduleJobCommand(data))
            .addRoute("^get ([\\p{L}\\p{N}\\p{ASCII}\\W]+) #([0-9]+) log",
                triggerWord+" get <project-name> #<build_number> log",
                "Return a truncated log for build #<build_number> of <project_name>",
                new GetProjectLogCommand(data));

            SlackTextMessage msg = router.route(commandText);

            return new JsonResponse(msg, StaplerResponse.SC_OK);
            
        } catch (RouteNotFoundException ex) {

            LOGGER.warning(ex.getMessage());

            String command = ex.getRouteCommand();

            String response = "*Help:*\n";
            if (command.split("\\s+").length > 1)
                response += "`"+command+"` _is an unknown command, try one of the following:_\n\n";
            else
                response += "\n";

            for (CommandRouter.Route route : router.getRoutes()) {
                response += "`"+route.command+"`\n```"+route.commandDescription+"```";
                response += "\n\n";
            }

            return new JsonResponse(new SlackTextMessage(response), StaplerResponse.SC_OK);

        } catch (CommandRouterException ex) {
            LOGGER.warning(ex.getMessage());
            return new JsonResponse(new SlackTextMessage(ex.getMessage()), StaplerResponse.SC_OK);

        } catch (Exception ex) {
            LOGGER.warning(ex.getMessage());
            return new JsonResponse(new SlackTextMessage("An error occured: "+ ex.getMessage()), StaplerResponse.SC_OK);
        }
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }
}
