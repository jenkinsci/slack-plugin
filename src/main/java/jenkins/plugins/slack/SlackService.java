package jenkins.plugins.slack;

import hudson.FilePath;
import hudson.model.TaskListener;
import net.sf.json.JSONArray;

public interface SlackService {
    boolean publish(String message);

    boolean publish(SlackRequest slackRequest);

    boolean publish(String message, String color);

    boolean publish(String message, String color, String timestamp);

    boolean publish(String message, JSONArray attachments, String color);

    boolean publish(String message, JSONArray attachments, String color, String timestamp);

    boolean upload(FilePath workspace, String artifactIncludes, TaskListener log);

    boolean addReaction(String channelId, String timestamp, String emojiName);

    String getResponseString();
}
