package jenkins.plugins.slack;

import java.util.Objects;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

public class SlackMessageRequest implements SlackRequest {
    private String message;
    private String color;
    private String timestamp;
    private String iconEmoji;
    private String username;
    private JSONArray attachments;
    private JSONArray blocks;
    private boolean replyBroadcast;
    private boolean asUser;

    private SlackMessageRequest(String message, String color, JSONArray attachments, JSONArray blocks, String timestamp, boolean replyBroadcast, boolean asUser, String iconEmoji, String username) {
        if (blocks != null && color != null) {
            throw new IllegalArgumentException("Color is not supported when blocks are set");
        }

        this.message = message;
        this.color = color;
        this.attachments = attachments;
        this.blocks = blocks;
        this.timestamp = timestamp;
        this.replyBroadcast = replyBroadcast;
        this.asUser = asUser;
        this.iconEmoji = iconEmoji;
        this.username = username;
    }

    public static SlackMessageRequestBuilder builder() {
        return new SlackMessageRequestBuilder();
    }

    public String getApiEndpoint() {
        if (StringUtils.isNotEmpty(timestamp)) {
            return "chat.update";
        }

        return "chat.postMessage";
    }

    public JSONObject getBody(String roomId) {
        JSONObject json = new JSONObject();

        //thread_ts is passed once with roomId: Ex: roomId:threadTs
        String[] splitThread = roomId.split("[:]+");
        if (splitThread.length > 1) {
            roomId = splitThread[0];
            json.put("channel", roomId);
            String threadTs = splitThread[1];
            if (threadTs.length() > 1) {
                json.put("thread_ts", threadTs);
            }
        } else {
            json.put("channel", roomId);
        }

        if (StringUtils.isNotEmpty(message)) {
            json.put("text", message);
        }
        if (attachments != null && !attachments.isEmpty()) {
            json.put("attachments", attachments);
        }

        if (blocks != null && !blocks.isEmpty()) {
            json.put("blocks", blocks);
        }
        json.put("link_names", "1");
        json.put("unfurl_links", "true");
        json.put("unfurl_media", "true");

        if (StringUtils.isNotEmpty(timestamp)) {
            json.put("ts", timestamp);
        }

        if (replyBroadcast) {
            json.put("replyBroadcast", "true");
        }

        if (asUser) {
            json.put("as_user", "true");
        }

        if (StringUtils.isNotEmpty(iconEmoji)) {
            json.put("icon_emoji", iconEmoji);
        }

        if (StringUtils.isNotEmpty(username)) {
            json.put("username", username);
        }

        return json;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getColor() {
        return color;
    }

    public JSONArray getAttachments() {
        return attachments;
    }

    public JSONArray getBlocks() {
        return blocks;
    }

    public boolean getReplyBroadcast() {
        return replyBroadcast;
    }

    public boolean getAsUser() {
        return asUser;
    }

    public String getIconEmoji() {
        return iconEmoji;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlackMessageRequest that = (SlackMessageRequest) o;
        return Objects.equals(message, that.message) &&
                Objects.equals(color, that.color) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(attachments, that.attachments) &&
                Objects.equals(blocks, that.blocks) &&
                Objects.equals(replyBroadcast, that.replyBroadcast) &&
                Objects.equals(asUser, that.asUser) &&
                Objects.equals(iconEmoji, that.iconEmoji) &&
                Objects.equals(username, that.username);
    }

    @Override
    public String toString() {
        return String.format("SlackMessageRequest{message='%s', color='%s', attachments=%s, blocks=%s, timestamp='%s'}", message, color, attachments, blocks, timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, color, attachments, blocks, timestamp, replyBroadcast, asUser, iconEmoji, username);
    }

    public static class SlackMessageRequestBuilder implements SlackRequestBuilder {
        private String message;
        private String color;
        private String timestamp;
        private String iconEmoji;
        private String username;
        private JSONArray attachments;
        private JSONArray blocks;
        private boolean replyBroadcast;
        private boolean asUser;

        private SlackMessageRequestBuilder() {
        }

        public SlackMessageRequestBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        public SlackMessageRequestBuilder withColor(String color) {
            this.color = color;
            return this;
        }

        public SlackMessageRequestBuilder withTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SlackMessageRequestBuilder withAttachments(JSONArray attachments) {
            this.attachments = attachments;
            return this;
        }

        public SlackMessageRequestBuilder withBlocks(JSONArray blocks) {
            this.blocks = blocks;
            return this;
        }

        public SlackMessageRequestBuilder withReplyBroadcast(boolean replyBroadcast) {
            this.replyBroadcast = replyBroadcast;
            return this;
        }

        public SlackMessageRequestBuilder withAsUser(boolean asUser) {
            this.asUser = asUser;
            return this;
        }

        public SlackMessageRequestBuilder withIconEmoji(String iconEmoji) {
            this.iconEmoji = iconEmoji;
            return this;
        }

        public SlackMessageRequestBuilder withUsername(String username) {
            this.username = username;
            return this;
        }

        public SlackMessageRequest build() {
            return new SlackMessageRequest(message, color, attachments, blocks, timestamp, replyBroadcast, asUser, iconEmoji, username);
        }

    }
}
