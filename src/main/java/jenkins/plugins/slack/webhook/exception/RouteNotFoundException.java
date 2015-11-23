package jenkins.plugins.slack.webhook.exception;




public class RouteNotFoundException extends CommandRouterException {
    private String routeCommand;

    public RouteNotFoundException(String message, String routeCommand) {
        super(message);
        this.routeCommand = routeCommand;
    }

    public String getRouteCommand() {
        return routeCommand;
    }
}
