package jenkins.plugins.slack;

import java.util.List;

public interface SlackService {
    boolean publish(String message);

    boolean publish(String message, String color);

    boolean publish(String text, String message, String color);

    String getUserId(String email);

    List<SlackUser> getUserList();
}
