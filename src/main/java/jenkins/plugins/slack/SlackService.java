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

    /**
     * Remove an emoji reaction to a message.
     * @param channelId - Slack's internal channel id (i.e. what's returned in a `chat.postMessage` response)
     * @param timestamp - Timestamp identifying the message
     * @param emojiName - The name of the emoji to add in reaction to the message (no colons)
     *
     * @return boolean indicating whether the API request succeeded
     */
    boolean removeReaction(String channelId, String timestamp, String emojiName);

    String getResponseString();
}
