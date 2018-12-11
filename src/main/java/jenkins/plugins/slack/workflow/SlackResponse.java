package jenkins.plugins.slack.workflow;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.json.JSONObject;

import java.io.Serializable;

public class SlackResponse implements Serializable {
    private String channelId;
    private String ts;

    public SlackResponse(String slackResponseString) {
        if (!StringUtils.isEmpty(slackResponseString)) {
            JSONObject result = new JSONObject(slackResponseString);
            channelId = result.getString("channel");
            ts = result.getString("ts");
        }
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
}
