package jenkins.plugins.slack.workflow;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.json.JSONObject;

import java.io.Serializable;

public class SlackResponse implements Serializable {
    private String channelId;
    private String ts;

    public SlackResponse(String slackResponseString) {
        JSONObject result = new JSONObject(slackResponseString);
        channelId = result.getString("channel");
        ts = result.getString("ts");
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
        return channelId + ":" + ts;
    }
}
