package jenkins.plugins.slack;

import java.util.Objects;
import net.sf.json.JSONArray;

public class SlackRequest {
    private String message;
    private String color;
    private JSONArray attachments;
    private JSONArray blocks;

    private SlackRequest(String message, String color, JSONArray attachments, JSONArray blocks) {
        if (blocks != null && color != null) {
            throw new IllegalArgumentException("Color is not supported when blocks are set");
        }

        this.message = message;
        this.color = color;
        this.attachments = attachments;
        this.blocks = blocks;
    }

    public static SlackRequestBuilder builder() {
        return new SlackRequestBuilder();
    }

    public String getMessage() {
        return message;
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
                Objects.equals(attachments, that.attachments) &&
                Objects.equals(blocks, that.blocks);
    }

    @Override
    public String toString() {
        return String.format("SlackRequest{message='%s', color='%s', attachments=%s, blocks=%s}", message, color, attachments, blocks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, color, attachments, blocks);
    }

    public static class SlackRequestBuilder {
        private String message;
        private String color;
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

        public SlackRequestBuilder withAttachments(JSONArray attachments) {
            this.attachments = attachments;
            return this;
        }

        public SlackRequestBuilder withBlocks(JSONArray blocks) {
            this.blocks = blocks;
            return this;
        }

        public SlackRequest build() {
            return new SlackRequest(message, color, attachments, blocks);
        }

    }
}
