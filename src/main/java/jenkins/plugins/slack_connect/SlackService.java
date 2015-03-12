package jenkins.plugins.slack_connect;

public interface SlackService {
    void publish(String message);

    void publish(String message, String color);
}
