package jenkins.plugins.slack;


import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StandardSlackService implements SlackService {

    private static final Logger logger = Logger.getLogger(StandardSlackService.class.getName());

    private String host = "slack.com";
    private String teamDomain;
    private String token;
    private String[] roomIds;

    public StandardSlackService(String teamDomain, String token, String roomId) {
        super();
        this.teamDomain = teamDomain;
        this.token = token;
        this.roomIds = roomId.split("[,; ]+");
    }

    public boolean publish(String message) {
        return publish(message, "warning");
    }

    public boolean publish(String message, String color) {
        boolean result = true;
        for (String roomId : roomIds) {
            String url = "https://" + teamDomain + "." + host + "/services/hooks/jenkins-ci?token=" + token;
            logger.info("Posting: to " + roomId + " on " + teamDomain + " using " + url +": " + message + " " + color);
            CloseableHttpClient client = getHttpClient();
            HttpPost post = new HttpPost(url);
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

                List<NameValuePair> nvps = new ArrayList<NameValuePair>();
                nvps.add(new BasicNameValuePair("payload", json.toString()));

                post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));


                CloseableHttpResponse response = client.execute(post);

                int responseCode;
                try {
                    responseCode = response.getStatusLine().getStatusCode();
                } finally {
                    response.close();
                }

                if(responseCode != HttpStatus.SC_OK) {
                    InputStream is = post.getEntity().getContent();

                    StringBuilder responseBuilder = new StringBuilder();
                    int data = is.read();
                    while(data != -1) {
                        responseBuilder.append((char)data);
                        data = is.read();
                    }

                    String responseString = responseBuilder.toString();
                    logger.log(Level.WARNING, "Slack post may have failed. Response: " + responseString);
                    logger.log(Level.WARNING, "Response Code: " + responseCode);
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
                try {
                    client.close();
                }catch (IOException e) {
                    logger.warning("Failed to close CloseableHttpClient: " + e.getLocalizedMessage());
                }
            }
        }
        return result;
    }

    protected CloseableHttpClient getHttpClient() {
        final HttpClientBuilder clientBuilder = HttpClients.custom();
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        if (Jenkins.getInstance() != null) {
            ProxyConfiguration proxy = Jenkins.getInstance().proxy;
            if (proxy != null) {
                final HttpHost proxyHost = new HttpHost(proxy.name, proxy.port);
                final HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
                clientBuilder.setRoutePlanner(routePlanner);


                String username = proxy.getUserName();
                String password = proxy.getPassword();
                // Consider it to be passed if username specified. Sufficient?
                if (username != null && !"".equals(username.trim())) {
                    logger.info("Using proxy authentication (user=" + username + ")");
                    credentialsProvider.setCredentials(new AuthScope(proxyHost), new UsernamePasswordCredentials(username, password));
                }
            }
        }
        return clientBuilder.build();
    }

    void setHost(String host) {
        this.host = host;
    }
}
