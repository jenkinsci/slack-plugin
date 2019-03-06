package jenkins.plugins.slack;

import net.sf.json.JSONArray;

public interface SlackService {
    boolean publish(String message);

    boolean publish(String message, String color);

    boolean publish(String message, JSONArray attachments, String color);

    String getResponseString();
}
