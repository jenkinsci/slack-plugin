package jenkins.plugins.slack.webhook;

import jenkins.plugins.slack.webhook.exception.CommandRouterException;
import jenkins.plugins.slack.webhook.exception.RouteNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandRouter<T> {

    public CommandRouter() {
    }

    public List<Route<T>> routes = new ArrayList<>();

    public CommandRouter<T> addRoute(String regex,
                                     String command,
                                     String commandDescription,
                                     RouterCommand<T> routerCommand) {

        this.routes.add(new CommandRouter.Route<>(regex,
                command,
                commandDescription,
                routerCommand));

        return this;
    }

    public List<Route<T>> getRoutes() {
        return this.routes;
    }

    public T route(String command) throws CommandRouterException {

        T message;
        for (Route<T> pa : routes) {

            Matcher matcher = pa.regex.matcher(command);

            boolean matches = matcher.matches();

            if (matches) {

                String[] parametersArray;

                if (matcher.groupCount() == 0) {
                    parametersArray = new String[]{command};
                } else {
                    parametersArray = new String[matcher.groupCount()];

                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        parametersArray[i - 1] = matcher.group(i);
                    }
                }

                try {
                    message = pa.routerCommand.execute(parametersArray);
                } catch (Exception ex) {
                    throw new CommandRouterException(ex.getMessage());
                }

                if (message == null) {
                    throw new RouteNotFoundException("No route found for given command", command);
                }

                return message;
            }
        }

        return null;
    }

    public static class Route<T> {
        public Pattern regex;
        public String command;
        public String commandDescription;
        public RouterCommand<T> routerCommand;

        public Route(String regex,
                     String command,
                     String commandDescription,
                     RouterCommand<T> routerCommand) {

            this.regex = Pattern.compile(regex);
            this.routerCommand = routerCommand;
            this.command = command;
            this.commandDescription = commandDescription;
        }
    }
}
