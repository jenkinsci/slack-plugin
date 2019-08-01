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
package jenkins.plugins.slack;

import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import hudson.tasks.MailAddressResolver;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jenkins.plugins.slack.user.SlackUserProperty;
import jenkins.scm.RunWithSCM;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class SlackUserIdResolver {

    private static final Logger LOGGER = Logger.getLogger(SlackUserIdResolver.class.getName());

    private static final String LOOKUP_BY_EMAIL_METHOD_URL_FORMAT = "https://slack.com/api/users.lookupByEmail?token=%s&email=%s";
    private static final String SLACK_OK_FIELD = "ok";
    private static final String SLACK_USER_FIELD = "user";
    private static final String SLACK_ID_FIELD = "id";

    private final String authToken;
    private final CloseableHttpClient httpClient;
    private final List<MailAddressResolver> mailAddressResolvers;

    public SlackUserIdResolver(String authToken, CloseableHttpClient httpClient, List<MailAddressResolver> mailAddressResolvers) {
        this.authToken = authToken;
        this.httpClient = httpClient;
        this.mailAddressResolvers = mailAddressResolvers != null ? mailAddressResolvers : MailAddressResolver.all();
    }

    public SlackUserIdResolver(String authToken, CloseableHttpClient httpClient) {
        this(authToken, httpClient, null);
    }

    @SuppressWarnings("unchecked")
    public List<String> resolveUserIdsForRun(Run run) {
        if (run instanceof RunWithSCM) {
            RunWithSCM r = (RunWithSCM) run;
            return resolveUserIdsForChangeLogSets(r.getChangeSets());
        } else if (run instanceof AbstractBuild) {
            AbstractBuild build = (AbstractBuild) run;
            return resolveUserIdsForChangeLogSets(build.getChangeSets());
        } else {
            return Collections.emptyList();
        }
    }

    public List<String> resolveUserIdsForChangeLogSet(ChangeLogSet changeLogSet) {
        return Arrays.stream(changeLogSet.getItems())
                .map(item -> resolveUserId(((Entry) item).getAuthor()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<String> resolveUserIdsForChangeLogSets(List<ChangeLogSet> changeLogSets) {
        return changeLogSets.stream()
                .map(this::resolveUserIdsForChangeLogSet)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public String resolveUserId(User user) {
        String userId = null;
        SlackUserProperty userProperty = user.getProperty(SlackUserProperty.class);
        if (userProperty != null) {
            userId = userProperty.getUserId();
        } else {
            userProperty = new SlackUserProperty(null, false);
        }

        if (StringUtils.isEmpty(userId)) {
            userId = mailAddressResolvers.stream()
                .map(resolver -> resolver.findMailAddressFor(user))
                .filter(emailAddress -> StringUtils.isNotEmpty(emailAddress))
                .map(this::resolveUserIdForEmailAddress)
                .filter(id -> StringUtils.isNotEmpty(id))
                .findAny()
                .orElse(null);

            userProperty.setUserId(userId);
            try {
                user.addProperty(userProperty);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Failed to add SlackUserProperty to user: " + user.toString(), ex);
            }
        }

        if (userProperty.getDisableNotifications()) {
            userId = null;
        }

        return userId;
    }

    public String resolveUserIdForEmailAddress(String emailAddress) {
        if (StringUtils.isEmpty(emailAddress) || StringUtils.isEmpty(authToken)) {
            return null;
        }

        String slackUserId = null;
        String url = String.format(LOOKUP_BY_EMAIL_METHOD_URL_FORMAT, authToken, emailAddress);
        HttpGet getRequest = new HttpGet(url);
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
}
