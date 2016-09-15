package jenkins.plugins.slack;

import jenkins.plugins.slack.util.HttpClientUtil;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class IncomingHookSlackService implements SlackService {
    private static final Logger LOGGER = Logger.getLogger(IncomingHookSlackService.class.getName());

    private final String webHookUrl;

    public IncomingHookSlackService(String webHookUrl) {
        this.webHookUrl = webHookUrl;
    }

    public boolean publish(String message) {
        return publish(message, "warning");
    }

    public boolean publish(String message, String color) {
        boolean result = true;
        LOGGER.info("Posting: to " + webHookUrl +": " + message + " " + color);
        HttpClient client = getHttpClient();
        PostMethod post = new PostMethod(webHookUrl);
        JSONObject json = new JSONObject();

        try {
            JSONObject field = new JSONObject();
            field.put("text", message);

            post.addParameter("payload", json.toString());
            post.getParams().setContentCharset("UTF-8");
            int responseCode = client.executeMethod(post);
            String response = post.getResponseBodyAsString();
            if (responseCode != HttpStatus.SC_OK) {
                LOGGER.log(Level.WARNING, "Slack post may have failed. Response: " + response);
                result = false;
            }
            else {
                LOGGER.info("Posting succeeded");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error posting to Slack", e);
            result = false;
        } finally {
            post.releaseConnection();
        }
        return result;
    }

    protected HttpClient getHttpClient() {
        return HttpClientUtil.getHttpClient();
    }
}
