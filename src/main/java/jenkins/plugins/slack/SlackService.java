package jenkins.plugins.slack;

import net.sf.json.JSONArray;

public interface SlackService {
    boolean publish(String message);

    boolean publish(SlackRequest slackRequest);

    boolean publish(String message, String color);

    boolean publish(String message, String color, String updateMessage);

    boolean publish(String message, JSONArray attachments, String color);

    boolean publish(String message, JSONArray attachments, String color, String updateMessage);

    String getResponseString();
}
