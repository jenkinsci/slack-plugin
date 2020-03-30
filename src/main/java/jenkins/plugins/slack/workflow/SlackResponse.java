package jenkins.plugins.slack.workflow;

import java.io.Serializable;
import jenkins.plugins.slack.SlackService;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.json.JSONObject;

public class SlackResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String THREAD_ID = "ts";
    private static final String CHANNEL = "channel";

    private transient SlackService slackService;
    private String channelId;
    private String ts;

    public SlackResponse(SlackService slackService) {
        this.slackService = slackService;
    }

    public SlackResponse(JSONObject slackResponseObject, SlackService slackService) {
        if (slackResponseObject.has(CHANNEL)) {
            channelId = slackResponseObject.getString(CHANNEL);
        }
        if (slackResponseObject.has(THREAD_ID)) {
            this.ts = slackResponseObject.getString(THREAD_ID);
        }

        this.slackService = slackService;
    }

    @Whitelisted
    public String getChannelId() {
        return channelId;
    }

    @Whitelisted
    public String getTs() {
        return ts;
    }

    @Whitelisted
    public String getThreadId() {
        if (!StringUtils.isEmpty(channelId) && !StringUtils.isEmpty(ts)) {
            return channelId + ":" + ts;
        } else {
            return null;
        }
    }

    /**
     * Add an emoji reaction to the message that this `SlackResponse` points to.
     *
     * @param emojiName - name of the emoji (no colons), e.g. `thumbsup`
     *
     * @return boolean indicating whether the API request succeeded
     */
    @Whitelisted
    public boolean addReaction(String emojiName) {
        if (slackService == null) return false;
        return slackService.addReaction(channelId, ts, emojiName);
    }
}
