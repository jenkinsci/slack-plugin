package jenkins.plugins.slack.webhook;


import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import java.util.UUID;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import jenkins.model.GlobalConfiguration;
import jenkins.plugins.slack.webhook.exception.CommandRouterException;
import jenkins.plugins.slack.webhook.exception.RouteNotFoundException;
import jenkins.plugins.slack.webhook.model.JsonResponse;
import jenkins.plugins.slack.webhook.model.SlackPostData;
import jenkins.plugins.slack.webhook.model.SlackTextMessage;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;


@Extension
public class WebhookEndpoint implements UnprotectedRootAction {

    private GlobalConfig globalConfig;

    private static final Logger LOGGER =
        Logger.getLogger(WebhookEndpoint.class.getName());

    private GlobalConfig getGlobalConfig(){
        if (globalConfig == null) {
            this.globalConfig = GlobalConfiguration.all().get(GlobalConfig.class);
        }
        return globalConfig;
    }

    @Override
    public String getUrlName() {
        String url = getGlobalConfig().getSlackOutgoingWebhookURL();
        if (url == null || url.equals(""))
            return UUID.randomUUID().toString().replaceAll("-", "");

        return url;
    }

    @RequirePOST
    public HttpResponse doIndex(StaplerRequest req) throws ServletException {

        if (getGlobalConfig().getSlackOutgoingWebhookToken() == null ||
            getGlobalConfig().getSlackOutgoingWebhookToken().equals("")) {
            return new JsonResponse(new SlackTextMessage("Slack token not set"), StaplerResponse.SC_OK);
        }

        SlackPostData data = req.bindJSON(SlackPostData.class, req.getSubmittedForm());

        if (!getGlobalConfig().getSlackOutgoingWebhookToken().equals(data.getToken()))
            return new JsonResponse(new SlackTextMessage("Invalid Slack token"), StaplerResponse.SC_OK);

        String commandText = data.getText();
        if (commandText == null || commandText.isEmpty())
            return new JsonResponse(new SlackTextMessage("Invalid command, text field required"),
                StaplerResponse.SC_OK);

        String triggerWord = data.getTrigger_word();
        if (triggerWord != null && ! triggerWord.isEmpty()) {
            // A trigger word is present, which is the case when Slack "outgoing webhooks" are used,
            // as opposed to "slash commands", when the trigger word is absent
            if (!commandText.startsWith(triggerWord))
                return new JsonResponse(new SlackTextMessage("Invalid command, invalid trigger_word"),
                        StaplerResponse.SC_OK);
            commandText = commandText.trim().replaceFirst(triggerWord, "").trim();
        }

        CommandRouter<SlackTextMessage> router = new CommandRouter<>();

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

            StringBuilder builder = new StringBuilder("*Help:*\n");
            if (command.split("\\s+").length > 1) {
                builder.append("`")
                        .append(command)
                        .append("` _is an unknown command, try one of the following:_\n\n");
            } else {
                builder.append("\n");
            }

            for (CommandRouter.Route route : router.getRoutes()) {
                builder.append("`")
                        .append(route.command)
                        .append("`\n```")
                        .append(route.commandDescription)
                        .append("```")
                        .append("\n\n");
            }

            return new JsonResponse(new SlackTextMessage(builder.toString()), StaplerResponse.SC_OK);

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
