package jenkins.plugins.slack;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import hudson.ProxyConfiguration;

public class StandardSlackService implements SlackService {

    private static final Logger logger = Logger.getLogger(StandardSlackService.class.getName());

    private String host = "api.slack.com";
    private String token;
    private String[] roomIds;
    private String from;

    public StandardSlackService(String token, String roomId, String from) {
        super();
        this.token = token;
        this.roomIds = roomId.split(",");
        this.from = from;
    }

    public void publish(String message) {
        publish(message, "yellow");
    }

    public void publish(String message, String color) {
        for (String roomId : roomIds) {
            logger.info("Posting: " + from + " to " + roomId + ": " + message + " " + color);
            HttpClient client = getHttpClient();
            String url = "https://" + host + "/v1/rooms/message?auth_token=" + token;
            PostMethod post = new PostMethod(url);

            try {
                post.addParameter("from", from);
                post.addParameter("room_id", roomId);
                post.addParameter("message", message);
                post.addParameter("color", color);
                post.addParameter("notify", shouldNotify(color));
                post.getParams().setContentCharset("UTF-8");
                int responseCode = client.executeMethod(post);
                String response = post.getResponseBodyAsString();
                if(responseCode != HttpStatus.SC_OK || ! response.contains("\"sent\"")) {
                    logger.log(Level.WARNING, "Slack post may have failed. Response: " + response);
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error posting to Slack", e);
            } finally {
                post.releaseConnection();
            }
        }
    }
    
    private HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        if (Jenkins.getInstance() != null) {
            ProxyConfiguration proxy = Jenkins.getInstance().proxy;
            if (proxy != null) {
                client.getHostConfiguration().setProxy(proxy.name, proxy.port);
            }
        }
        return client;
    }

    private String shouldNotify(String color) {
        return color.equalsIgnoreCase("green") ? "0" : "1";
    }

    void setHost(String host) {
        this.host = host;
    }
}
