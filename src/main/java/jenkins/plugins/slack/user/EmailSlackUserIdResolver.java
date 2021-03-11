/*
 * The MIT License
 *
 * Copyright (C) 2019 Electronic Arts Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.plugins.slack.user;

import hudson.Extension;
import hudson.model.User;
import hudson.tasks.MailAddressResolver;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

public class EmailSlackUserIdResolver extends SlackUserIdResolver {

    private static final Logger LOGGER = Logger.getLogger(EmailSlackUserIdResolver.class.getName());

    private static final String AUTHORIZATION_BEARER_TOKEN_FORMAT = "Bearer %s";
    private static final String LOOKUP_BY_EMAIL_METHOD_URL = "https://slack.com/api/users.lookupByEmail";
    private static final String LOOKUP_BY_EMAIL_METHOD_URL_FORMAT = LOOKUP_BY_EMAIL_METHOD_URL + "?email=%s";
    private static final String SLACK_OK_FIELD = "ok";
    private static final String SLACK_USER_FIELD = "user";
    private static final String SLACK_ID_FIELD = "id";

    private String authToken;
    private CloseableHttpClient httpClient;
    private List<MailAddressResolver> mailAddressResolvers;

    public EmailSlackUserIdResolver(String authToken, CloseableHttpClient httpClient, List<MailAddressResolver> mailAddressResolvers) {
        this.authToken = authToken;
        this.httpClient = httpClient;
        this.mailAddressResolvers = mailAddressResolvers != null ? mailAddressResolvers : MailAddressResolver.all();
    }

    public EmailSlackUserIdResolver(String authToken, CloseableHttpClient httpClient) {
        this(authToken, httpClient, null);
    }

    @DataBoundConstructor
    public EmailSlackUserIdResolver() {
        this(null, null, null);
    }

    public String getAPIMethodURL() {
        return LOOKUP_BY_EMAIL_METHOD_URL;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setMailAddressResolvers(List<MailAddressResolver> mailAddressResolvers) {
        this.mailAddressResolvers = mailAddressResolvers;
    }

    protected String resolveUserId(User user) {
        if (mailAddressResolvers == null) {
            LOGGER.fine("No mail address resolver found");
            return null;
        }

       return mailAddressResolvers.stream()
            .map(resolver -> {
                try {
                    return resolver.findMailAddressFor(user);
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, String.format(
                        "The email resolver '%s' failed", resolver.getClass().getName()), ex);
                    return null;
              }
            })
            .filter(StringUtils::isNotEmpty)
            .map(this::resolveUserIdForEmailAddress)
            .filter(StringUtils::isNotEmpty)
            .findAny()
            .orElse(null);
    }

    public String resolveUserIdForEmailAddress(String emailAddress) {
        if (StringUtils.isEmpty(emailAddress)) {
            LOGGER.fine("Email address was empty");
            return null;
        }

        if (StringUtils.isEmpty(authToken)) {
            LOGGER.fine("Auth token was empty");
            return null;
        }

        String slackUserId = null;
        String url = String.format(LOOKUP_BY_EMAIL_METHOD_URL_FORMAT, emailAddress);
        HttpGet getRequest = new HttpGet(url);
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, String.format(AUTHORIZATION_BEARER_TOKEN_FORMAT, authToken));
        try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
            int responseCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == responseCode) {
                HttpEntity entity = response.getEntity();
                JSONObject slackResponse = new JSONObject(EntityUtils.toString(entity));
                // additionally, make sure the JSON response contains an 'ok: true' entry
                if (slackResponse.optBoolean(SLACK_OK_FIELD)) {
                    JSONObject slackUser = slackResponse.getJSONObject(SLACK_USER_FIELD);
                    slackUserId = slackUser.getString(SLACK_ID_FIELD);
                }
            }
        } catch (IOException | ParseException | JSONException ex) {
            LOGGER.log(Level.WARNING, "Error getting userId from Slack", ex);
        }
        return slackUserId;
    }

    @Extension
    public static class DescriptorImpl extends SlackUserIdResolverDescriptor {

        @Override
        public String getDisplayName() {
            return "Slack email User ID Resolver";
        }
    }
}
