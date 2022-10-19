package jenkins.plugins.slack;

import java.util.Objects;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

public class SlackRequest {
    private final String message;
    private final String color;
    private final String timestamp;
    private final JSONArray attachments;
    private final JSONArray blocks;

    private SlackRequest(String message, String color, JSONArray attachments, JSONArray blocks, String timestamp) {
        if ((blocks != null && attachments == null) && color != null) {
            throw new IllegalArgumentException("Color is not supported when blocks are set");
        }

        this.message = message;
        this.color = color;
        this.attachments = attachments;
        this.blocks = blocks;
        this.timestamp = timestamp;
    }

    public static SlackRequestBuilder builder() {
        return new SlackRequestBuilder();
    }

    public JSONObject getBody() {
        JSONObject json = new JSONObject();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlackRequest that = (SlackRequest) o;
        return Objects.equals(message, that.message) &&
                Objects.equals(color, that.color) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(attachments, that.attachments) &&
                Objects.equals(blocks, that.blocks);
    }

    @Override
    public String toString() {
        return String.format("SlackRequest{message='%s', color='%s', attachments=%s, blocks=%s, timestamp='%s'}", message, color, attachments, blocks, timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, color, attachments, blocks, timestamp);
    }

    public static class SlackRequestBuilder {
        private String message;
        private String color;
        private String timestamp;
        private JSONArray attachments;
        private JSONArray blocks;

        private SlackRequestBuilder() {
        }

        public SlackRequestBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        public SlackRequestBuilder withColor(String color) {
            this.color = color;
            return this;
        }

        public SlackRequestBuilder withTimestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SlackRequestBuilder withAttachments(JSONArray attachments) {
            this.attachments = attachments;
            return this;
        }

        public SlackRequestBuilder withBlocks(JSONArray blocks) {
            this.blocks = blocks;
            return this;
        }

        public SlackRequest build() {
            return new SlackRequest(message, color, attachments, blocks, timestamp);
        }

    }
}
