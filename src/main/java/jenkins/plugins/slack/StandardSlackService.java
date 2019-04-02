package jenkins.plugins.slack;

import hudson.ProxyConfiguration;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
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
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

public class StandardSlackService implements SlackService {

    private static final Logger logger = Logger.getLogger(StandardSlackService.class.getName());

    private String host = "slack.com";
    private String baseUrl;
    private String teamDomain;
    private boolean botUser;
    private String[] roomIds;
    private boolean replyBroadcast;
    private String responseString;
    private String populatedToken;

    /**
     * @deprecated use {@link #StandardSlackService(String, String, boolean, String, boolean, String)} instead}
     */
    @Deprecated
    public StandardSlackService(String baseUrl, String teamDomain, String authTokenCredentialId, boolean botUser, String roomId) {
        this(baseUrl, teamDomain, null, authTokenCredentialId, botUser, roomId, false);
    }

    /**
     * @deprecated use {@link #StandardSlackService(String, String, boolean, String, boolean, String)} instead}
     */
    @Deprecated
    public StandardSlackService(String baseUrl, String teamDomain, String token, String authTokenCredentialId, boolean botUser, String roomId) {
        this(baseUrl, teamDomain, token, authTokenCredentialId, botUser, roomId, false);
    }

    /**
     * @deprecated use {@link #StandardSlackService(String, String, boolean, String, boolean, String)} instead}
     */
    @Deprecated
    public StandardSlackService(String baseUrl, String teamDomain, String token, String authTokenCredentialId, boolean botUser, String roomId, boolean replyBroadcast) {
        this(baseUrl, teamDomain, botUser, roomId, replyBroadcast);
        this.populatedToken = getTokenToUse(authTokenCredentialId, token);
        if (this.populatedToken == null) {
            throw new IllegalArgumentException("No slack token found, setup a secret text credential and configure it to be used");
        }
    }

    /**
     * @param baseUrl        the full url to use, this is an alternative to specifying teamDomain
     * @param teamDomain     the teamDomain inside slack.com to use
     * @param botUser
     * @param roomId         a semicolon separated list of rooms to notify
     * @param replyBroadcast
     * @param populatedToken a non-null token to use for authentication
     */
    public StandardSlackService(String baseUrl, String teamDomain, boolean botUser, String roomId, boolean replyBroadcast, String populatedToken) {
        this(baseUrl, teamDomain, botUser, roomId, replyBroadcast);
        if (populatedToken == null) {
            throw new IllegalArgumentException("No slack token found, setup a secret text credential and configure it to be used");
        }
        this.populatedToken = populatedToken;
    }

    private StandardSlackService(String baseUrl, String teamDomain, boolean botUser, String roomId, boolean replyBroadcast) {
        super();
        this.baseUrl = baseUrl;
        if (this.baseUrl != null && !this.baseUrl.isEmpty() && !this.baseUrl.endsWith("/")) {
            this.baseUrl += "/";
        }
        this.teamDomain = teamDomain;
        this.botUser = botUser;
        if (roomId == null) {
            throw new IllegalArgumentException("Project Channel or Slack User ID must be specified.");
        }
        this.roomIds = roomId.split("[,; ]+");
        this.replyBroadcast = replyBroadcast;
    }

    public String getResponseString() {
        return responseString;
    }

    public boolean publish(String message) {
        return publish(message, "warning");
    }

    public boolean publish(String message, String color) {
        //prepare attachments first
        JSONObject field = new JSONObject();
        field.put("short", false);
        field.put("value", message);

        JSONArray fields = new JSONArray();
        fields.add(field);

        JSONObject attachment = new JSONObject();
        attachment.put("fallback", message);
        attachment.put("color", color);
        attachment.put("fields", fields);
        JSONArray mrkdwn = new JSONArray();
        mrkdwn.add("pretext");
        mrkdwn.add("text");
        mrkdwn.add("fields");
        attachment.put("mrkdwn_in", mrkdwn);
        JSONArray attachments = new JSONArray();
        attachments.add(attachment);

        return publish(null, attachments, color);
    }

    @Override
    public boolean publish(String message, JSONArray attachments, String color) {
        boolean result = true;
        for (String roomId : roomIds) {
            HttpPost post;
            String url;
            String threadTs = "";
            List<NameValuePair> nvps = new ArrayList<>();

            //thread_ts is passed once with roomId: Ex: roomId:threadTs
            String[] splitThread = roomId.split("[:]+");
            if (splitThread.length > 1) {
                roomId = splitThread[0];
                threadTs = splitThread[1];
            }
            //prepare post methods for both requests types
            if (!botUser || !StringUtils.isEmpty(baseUrl)) {
                url = "https://" + teamDomain + "." + host + "/services/hooks/jenkins-ci?token=" + populatedToken;
                if (!StringUtils.isEmpty(baseUrl)) {
                    url = baseUrl + populatedToken;
                }
                post = new HttpPost(url);
                JSONObject json = new JSONObject();

                json.put("channel", roomId);
                if (StringUtils.isNotEmpty(message)) {
                    json.put("text", message);
                }
                json.put("attachments", attachments);
                json.put("link_names", "1");

                nvps.add(new BasicNameValuePair("payload", json.toString()));
            } else {
                url = "https://slack.com/api/chat.postMessage?token=" + populatedToken +
                        "&channel=" + roomId.replace("#", "") +
                        "&link_names=1" +
                        "&as_user=true";
                if (threadTs.length() > 1) {
                    url += "&thread_ts=" + threadTs;
                }
                if (replyBroadcast) {
                    url += "&reply_broadcast=true";
                }
                try {
                    url += "&attachments=" + URLEncoder.encode(attachments.toString(), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    logger.log(Level.ALL, "Error while encoding attachments: " + e.getMessage());
                }
                post = new HttpPost(url);
            }
            logger.fine("Posting: to " + roomId + " on " + teamDomain + " using " + url + ": " + attachments.toString() + " " + color);
            CloseableHttpClient client = getHttpClient();

            try {
                post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
                CloseableHttpResponse response = client.execute(post);

                int responseCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                if (botUser && entity != null) {
                    responseString = EntityUtils.toString(entity);
                }
                if (responseCode != HttpStatus.SC_OK) {
                    logger.log(Level.WARNING, "Slack post may have failed. Response: " + responseString);
                    logger.log(Level.WARNING, "Response Code: " + responseCode);
                    result = false;
                } else {
                    logger.fine("Posting succeeded");
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

    private String getTokenToUse(String authTokenCredentialId, String token) {
        if (!StringUtils.isEmpty(authTokenCredentialId)) {
            StringCredentials credentials = CredentialsObtainer.lookupCredentials(authTokenCredentialId);
            if (credentials != null) {
                logger.fine("Using Integration Token Credential ID.");
                return credentials.getSecret().getPlainText();
            }
        }

        logger.fine("Using Integration Token.");
        return token;
    }

    protected CloseableHttpClient getHttpClient() {
        final HttpClientBuilder clientBuilder = HttpClients.custom();
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins != null) {
            ProxyConfiguration proxy = jenkins.proxy;
            if (proxy != null) {
                final HttpHost proxyHost = new HttpHost(proxy.name, proxy.port);
                final HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
                clientBuilder.setRoutePlanner(routePlanner);

                String username = proxy.getUserName();
                String password = proxy.getPassword();
                // Consider it to be passed if username specified. Sufficient?
                if (username != null && !"".equals(username.trim())) {
                    logger.info("Using proxy authentication (user=" + username + ")");
                    credentialsProvider.setCredentials(new AuthScope(proxyHost),
                            new UsernamePasswordCredentials(username, password));
                }
            }
        }
        return clientBuilder.build();
    }

    void setHost(String host) {
        this.host = host;
    }
}
