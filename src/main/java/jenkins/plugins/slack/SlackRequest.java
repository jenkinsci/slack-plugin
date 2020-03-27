package jenkins.plugins.slack;

import net.sf.json.JSONObject;

public interface SlackRequest {
    public String getApiEndpoint();

    public JSONObject getBody(String roomId);
}
