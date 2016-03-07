package jenkins.plugins.slack.webhook;


import java.util.List;
import java.util.ArrayList;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import jenkins.plugins.slack.webhook.exception.CommandRouterException;
import jenkins.plugins.slack.webhook.exception.RouteNotFoundException;




public class CommandRouter<T> {

    public CommandRouter() { }

    public List<Route<T>> routes = new ArrayList<Route<T>>();

    public CommandRouter<T> addRoute(String regex,
        String command,
        String commandDescription,
        RouterCommand<T> routerCommand) {

        this.routes.add(new CommandRouter.Route<T>(regex,
            command,
            commandDescription,
            routerCommand));

        return this;
    }

    public List<Route<T>> getRoutes() {
        return this.routes;
    }

    public T route(String command) throws CommandRouterException,
        RouteNotFoundException {

        T message = null;

        for (Route<T> pa : routes) {

            Matcher matcher = pa.regex.matcher(command);

            boolean matches = matcher.matches();

            if (matches) {
    
                String[] parametersArray = null;

                if (matcher.groupCount() == 0) {
                    parametersArray = new String[] { command };
                } else {
                    parametersArray = new String[matcher.groupCount()];

                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        parametersArray[i-1] = matcher.group(i);
                    }
                }
                
                try {
                    message = pa.routerCommand.execute(parametersArray);
                } catch (Exception ex) {
                    throw new CommandRouterException(ex.getMessage());
                }

                if (message == null)
                    throw new RouteNotFoundException("No route found for given command", command);

                return message;
            }
        }

        return message;
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
