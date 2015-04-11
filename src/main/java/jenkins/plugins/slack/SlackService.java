package jenkins.plugins.slack;

public interface SlackService {
    boolean publish(String message);

    boolean publish(String message, String color);
}
