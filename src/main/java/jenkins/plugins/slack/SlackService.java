package jenkins.plugins.slack;

import jenkins.plugins.slack.webhook.model.SlackUser;

import java.util.List;

public interface SlackService {
    boolean publish(String message);

    boolean publish(String message, String color);

    String getUserId(String email);

    List<SlackUser> getUserList();
}
