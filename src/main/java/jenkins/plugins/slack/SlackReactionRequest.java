package jenkins.plugins.slack;

import java.util.Objects;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

public class SlackReactionRequest {
    private final String channelId;
    private final String timestamp;
    private final String emojiName;

    private SlackReactionRequest(String channelId, String timestamp, String emojiName) {
        if (StringUtils.isEmpty(channelId) || StringUtils.isEmpty(timestamp) || StringUtils.isEmpty(emojiName)) {
            throw new IllegalArgumentException("Slack reaction requires all of channelId, timestamp, and emojiName");
        }

        this.channelId = channelId;
        this.timestamp = timestamp;
        this.emojiName = emojiName;
    }

    public static SlackReactionRequestBuilder builder() {
        return new SlackReactionRequestBuilder();
    }

    public JSONObject getBody() {
        JSONObject json = new JSONObject();
        json.put("channel", channelId);
        json.put("timestamp", timestamp);
        json.put("name", emojiName);
        return json;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getEmojiName() {
        return emojiName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlackReactionRequest that = (SlackReactionRequest) o;
        return Objects.equals(channelId, that.channelId) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(emojiName, that.emojiName);
    }

    @Override
    public String toString() {
        return String.format("SlackReactionRequest{channelId='%s', timestamp='%s', emojiName='%s'}", channelId, timestamp, emojiName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, timestamp, emojiName);
    }

    public static class SlackReactionRequestBuilder {
        private String channelId;
        private String timestamp;
        private String emojiName;

        private SlackReactionRequestBuilder() {
        }

        public SlackReactionRequestBuilder withChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public SlackReactionRequestBuilder withTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SlackReactionRequestBuilder withEmojiName(String emojiName) {
            this.emojiName = emojiName;
            return this;
        }

        public SlackReactionRequest build() {
            return new SlackReactionRequest(channelId, timestamp, emojiName);
        }

    }
}
