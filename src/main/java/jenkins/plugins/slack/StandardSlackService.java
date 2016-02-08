package jenkins.plugins.slack;

import hudson.security.ACL;

import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import hudson.security.ACL;
import jenkins.model.Jenkins;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;

import jenkins.model.Jenkins;
import hudson.ProxyConfiguration;

import org.apache.commons.lang.StringUtils;
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
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;

public class StandardSlackService implements SlackService {

    private static final Logger logger = Logger.getLogger(StandardSlackService.class.getName());

    private String host = "slack.com";
    private String baseUrl;
    private String teamDomain;
    private String token;
    private String authTokenCredentialId;
    private boolean botUser;
    private String[] roomIds;
    private String apiToken;

    public StandardSlackService(String baseUrl, String teamDomain, String token, String authTokenCredentialId, boolean botUser, String roomId, String apiToken) {
        super();
        this.baseUrl = baseUrl;
        if(this.baseUrl != null && !this.baseUrl.isEmpty() && !this.baseUrl.endsWith("/")) {
            this.baseUrl += "/";
        }
        this.teamDomain = teamDomain;
        this.token = token;
        this.authTokenCredentialId = StringUtils.trim(authTokenCredentialId);
        this.botUser = botUser;
        this.roomIds = roomId.split("[,; ]+");
        this.apiToken = apiToken;
    }

    public boolean publish(String message) {
        return publish(message, "warning");
    }

    public boolean publish(String message, String color) {
        boolean result = true;
        for (String roomId : roomIds) {
            //prepare attachments first
            JSONObject attachment = new JSONObject();
            attachment.put("text", message);
            attachment.put("fallback", message);
            attachment.put("color", color);

            JSONArray mrkdwn = new JSONArray();
            mrkdwn.put("pretext");
            mrkdwn.put("text");
            mrkdwn.put("fields");
            attachment.put("mrkdwn_in", mrkdwn);
            JSONArray attachments = new JSONArray();
            attachments.put(attachment);

            PostMethod post;
            String url;
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            //prepare post methods for both requests types
            if (!botUser || !StringUtils.isEmpty(baseUrl)) {
                url = "https://" + teamDomain + "." + host + "/services/hooks/jenkins-ci?token=" + getTokenToUse();
                if (!StringUtils.isEmpty(baseUrl)) {
                    url = baseUrl + getTokenToUse();
                }
                post = new PostMethod(url);
                JSONObject json = new JSONObject();

                json.put("channel", roomId);
                json.put("attachments", attachments);
                json.put("link_names", "1");

                post.addParameter("payload", json.toString());
                post.getParams().setContentCharset("UTF-8");
            } else {
                url = "https://slack.com/api/chat.postMessage?token=" + getTokenToUse() +
                        "&channel=" + roomId +
                        "&link_names=1" +
                        "&as_user=true";
                try {
                    url += "&attachments=" + URLEncoder.encode(attachments.toString(), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    logger.log(Level.ALL, "Error while encoding attachments: " + e.getMessage());
                }
                post = new PostMethod(url);
            }
            logger.fine("Posting: to " + roomId + " on " + teamDomain + " using " + url + ": " + message + " " + color);
            Client client = getClient();

            try {
            	ClientResponse response = client.request(post);

            	int responseCode = response.getStatusCode();
            	if(responseCode != HttpStatus.SC_OK) {
            		 String responseString = response.getBody();
            		 logger.log(Level.WARNING, "Slack post may have failed. Response: " + responseString);
            		 logger.log(Level.WARNING, "Response Code: " + responseCode);
            		 result = false;
                } else {
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

        if (apiToken.equals("")) {
            return null;
        }

        String url = "https://" + host + "/api/users.list?token=" + apiToken;
        logger.info("Getting: users list");
        Client client = getClient();
        GetMethod get = new GetMethod(url);
        ClientResponse response = null;
        try {
            response = client.request(get);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String responseBody = response.getBody();
        if (response.getStatusCode() != HttpStatus.SC_OK) {
            logger.log(Level.WARNING, "Slack get request may have failed. Response: " + responseBody);
            return null;
        }
        logger.info("Getting succeeded");
        JSONObject responseJSON = new JSONObject(responseBody);

        Boolean ok = responseJSON.getBoolean("ok");
        if (!ok) {
            String error = responseJSON.getString("error");
            logger.log(Level.WARNING, "Slack get request may have failed. Error: " + error);
            return null;
        }

        // TODO: Cache this somewhere
        JSONArray members = responseJSON.getJSONArray("members");
        for (int i = 0; i < members.length(); i++) {
            JSONObject member = members.getJSONObject(i);
            if (email.equals(member.getJSONObject("profile").optString("email"))) {
                return member.getString("id");
            }
        }
        return null;
    }

    private String getTokenToUse() {
        if (authTokenCredentialId != null && !authTokenCredentialId.isEmpty()) {
            StringCredentials credentials = lookupCredentials(authTokenCredentialId);
            if (credentials != null) {
                logger.fine("Using Integration Token Credential ID.");
                return credentials.getSecret().getPlainText();
            }
        }

        logger.fine("Using Integration Token.");

        return token;
    }

    private StringCredentials lookupCredentials(String credentialId) {
        List<StringCredentials> credentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(StringCredentials.class, Jenkins.getInstance(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
        CredentialsMatcher matcher = CredentialsMatchers.withId(credentialId);
        return CredentialsMatchers.firstOrNull(credentials, matcher);
    }

    protected Client getClient() {
        return new StandardClient();
    }

    void setHost(String host) {
        this.host = host;
    }
}
