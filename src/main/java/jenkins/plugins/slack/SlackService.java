package jenkins.plugins.slack;

import org.json.JSONArray;

public interface SlackService {
    boolean publish(String message);

    boolean publish(String message, String color);

    boolean publish(JSONArray attachments, String color);
}
