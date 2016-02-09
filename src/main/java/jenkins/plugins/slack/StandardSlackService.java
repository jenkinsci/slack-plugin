package jenkins.plugins.slack;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StandardSlackService implements SlackService {

    private static final Logger logger = Logger.getLogger(StandardSlackService.class.getName());

    private String host = "slack.com";
    private String teamDomain;
    private String token;
    private String[] roomIds;
    private String apiToken;

    public StandardSlackService(String teamDomain, String token, String roomId, String apiToken) {
        super();
        this.teamDomain = teamDomain;
        this.token = token;
        this.roomIds = roomId.split("[,; ]+");
        this.apiToken = apiToken;
    }

    public boolean publish(String message) {
        return publish(message, "warning");
    }

    public boolean publish(String message, String color) {
        boolean result = true;
        for (String roomId : roomIds) {
            String url = "https://" + teamDomain + "." + host + "/services/hooks/jenkins-ci?token=" + token;
            logger.info("Posting: to " + roomId + " on " + teamDomain + " using " + url +": " + message + " " + color);
            Client client = getClient();
            PostMethod post = new PostMethod(url);
            JSONObject json = new JSONObject();

            try {
                JSONObject field = new JSONObject();
                field.put("short", false);
                field.put("value", message);

                JSONArray fields = new JSONArray();
                fields.put(field);

                JSONObject attachment = new JSONObject();
                attachment.put("fallback", message);
                attachment.put("color", color);
                attachment.put("fields", fields);
                JSONArray mrkdwn = new JSONArray();
                mrkdwn.put("pretext");
                mrkdwn.put("text");
                mrkdwn.put("fields");
                attachment.put("mrkdwn_in", mrkdwn);
                JSONArray attachments = new JSONArray();
                attachments.put(attachment);

                json.put("channel", roomId);
                json.put("attachments", attachments);
                json.put("link_names", "1");

                post.addParameter("payload", json.toString());
                post.getParams().setContentCharset("UTF-8");
                ClientResponse response = client.request(post);
                if(response.getStatusCode() != HttpStatus.SC_OK) {
                    logger.log(Level.WARNING, "Slack post may have failed. Response: " + response.getBody());
                    result = false;
                }
                else {
                    logger.info("Posting succeeded");
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error posting to Slack", e);
                result = false;
            } finally {
                post.releaseConnection();
            }
        }
        return result;
    }

    public String getUserId(String email) {
        return "";
    }

    protected Client getClient() {
        return new StandardClient();
    }

    void setHost(String host) {
        this.host = host;
    }
}
