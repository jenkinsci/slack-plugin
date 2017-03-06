package jenkins.plugins.slack;

import hudson.security.ACL;

import org.json.JSONArray;
import org.json.JSONObject;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
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

    public StandardSlackService(String baseUrl, String teamDomain, String token, String authTokenCredentialId, boolean botUser, String roomId) {
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

            HttpPost post;
            String url;
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            //prepare post methods for both requests types
            if (!botUser || !StringUtils.isEmpty(baseUrl)) {
                url = "https://" + teamDomain + "." + host + "/services/hooks/jenkins-ci?token=" + getTokenToUse();
                if (!StringUtils.isEmpty(baseUrl)) {
                    url = baseUrl + getTokenToUse();
                }
                post = new HttpPost(url);
                JSONObject json = new JSONObject();

                json.put("channel", roomId);
                json.put("attachments", attachments);
                json.put("link_names", "1");

                nvps.add(new BasicNameValuePair("payload", json.toString()));
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
                post = new HttpPost(url);
            }
            logger.fine("Posting: to " + roomId + " on " + teamDomain + " using " + url + ": " + message + " " + color);
            CloseableHttpClient client = getHttpClient();

            try {
            	post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            	CloseableHttpResponse response = client.execute(post);
            	
            	int responseCode = response.getStatusLine().getStatusCode();
            	if(responseCode != HttpStatus.SC_OK) {
            		 InputStream is = response.getEntity().getContent();
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
