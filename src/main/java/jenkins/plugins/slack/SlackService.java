package jenkins.plugins.slack;

public interface SlackService {
    void publish(String message);

    void publish(String message, String color);
}
