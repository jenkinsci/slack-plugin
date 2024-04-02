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

import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import hudson.model.User;
import hudson.tasks.MailAddressResolver;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    private List<MailAddressResolver> mailAddressResolvers;
    private transient Function<User, String> defaultMailAddressResolver;

    @VisibleForTesting
    EmailSlackUserIdResolver(String authToken, CloseableHttpClient httpClient, List<MailAddressResolver> mailAddressResolvers, Function<User, String> defaultMailAddressResolver) {
        super(authToken, httpClient);
        this.mailAddressResolvers = mailAddressResolvers;
        this.defaultMailAddressResolver = defaultMailAddressResolver;
    }

    public EmailSlackUserIdResolver(String authToken, CloseableHttpClient httpClient, List<MailAddressResolver> mailAddressResolvers) {
        this(authToken, httpClient, mailAddressResolvers, MailAddressResolver::resolve);
    }

    public EmailSlackUserIdResolver(String authToken, CloseableHttpClient httpClient) {
        this(authToken, httpClient, null);
    }

    @DataBoundConstructor
    public EmailSlackUserIdResolver() {
        this(null, null);
    }

    public String getAPIMethodURL() {
        return LOOKUP_BY_EMAIL_METHOD_URL;
    }

    public void setMailAddressResolvers(List<MailAddressResolver> mailAddressResolvers) {
        this.mailAddressResolvers = mailAddressResolvers;
    }

    private String resolveUserEmail(User user){
        return Optional.ofNullable(mailAddressResolvers)
            .map(Collection::stream)
            .orElseGet(Stream::empty)
            .map(resolver -> {
                try {
                    String email = resolver.findMailAddressFor(user);
                    LOGGER.log(Level.FINEST, String.format(
                            "The email resolver '%s' resolved %s as %s", resolver.getClass().getName(), user.getId(), email));
                    return email;
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING, String.format(
                            "The email resolver '%s' failed", resolver.getClass().getName()), ex);
                    return null;
                }
            })
            .filter(StringUtils::isNotEmpty)
            .findAny()
            .orElse(null);
    }

    protected SlackUserProperty fetchUserSlackProperty(User user) {
        String userEmail = resolveUserEmail(user);

        SlackUserProperty userProperty = null;
        if(userEmail != null){
            userProperty = resolveSlackPropertyFromEmailViaSlack(userEmail);
        }

        // Return value can be null, so Optional.orElseGet(Supplier) doesn't work.
        if (userProperty != null) {
            return userProperty;
        } else if (defaultMailAddressResolver != null){
            return resolveSlackPropertyFromEmailViaSlack(defaultMailAddressResolver.apply(user));
        } else {
            return null;
        }
    }

    public String resolveUserIdForEmailAddress(String emailAddress) {
        SlackUserProperty userProperty = resolveSlackProperty(emailAddress);
        if(userProperty == null){
            return null;
        }
        if(userProperty.getDisableNotifications()){
            return null;
        }
        return userProperty.getUserId();
    }

    private SlackUserProperty resolveSlackProperty(String emailAddress) {
        if (StringUtils.isEmpty(emailAddress)) {
            LOGGER.fine("Email address was empty");
            return null;
        }

        SlackUserProperty userProperty = resolveSlackPropertyFromEmailViaSlack(emailAddress);
        if(userProperty == null) {
            userProperty = resolveSlackPropertyFromEmailViaInferredUsername(emailAddress);
        }
        if(userProperty == null) {
            userProperty = resolveSlackPropertyFromEmailViaUsers(emailAddress);
        }

        return userProperty;
    }

    private SlackUserProperty resolveSlackPropertyFromEmailViaSlack(String emailAddress){
        if (StringUtils.isEmpty(authToken)) {
            LOGGER.fine("Auth token was empty");
            return null;
        }
        if (StringUtils.isEmpty(emailAddress)) {
            LOGGER.fine("Auth token was empty");
            return null;
        }

        String slackUserId = null;
        final String url = String.format(LOOKUP_BY_EMAIL_METHOD_URL_FORMAT, emailAddress);
        final HttpGet getRequest = new HttpGet(url);
        getRequest.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.getMimeType());
        getRequest.addHeader(HttpHeaders.AUTHORIZATION, String.format(AUTHORIZATION_BEARER_TOKEN_FORMAT, authToken));
        try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
            final int responseCode = response.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == responseCode) {
                final HttpEntity entity = response.getEntity();
                final JSONObject slackResponse = new JSONObject(EntityUtils.toString(entity));
                // additionally, make sure the JSON response contains an 'ok: true' entry
                if (slackResponse.optBoolean(SLACK_OK_FIELD)) {
                    final JSONObject slackUser = slackResponse.getJSONObject(SLACK_USER_FIELD);
                    slackUserId = slackUser.getString(SLACK_ID_FIELD);
                }
            }
        } catch (IOException | ParseException | JSONException ex) {
            LOGGER.log(Level.WARNING, "Error getting userId from Slack", ex);
        }

        SlackUserProperty userProperty = null;
        if(slackUserId != null){
            LOGGER.fine(String.format("Found Slack ID '%s' for email '%s'", slackUserId, emailAddress));
            userProperty = new SlackUserProperty();
            userProperty.setDisableNotifications(false);
            userProperty.setUserId(slackUserId);
        } else {
            LOGGER.log(Level.INFO, String.format("Failed to resolve userId from Slack from email '%s'", emailAddress));
        }
        return userProperty;
    }

    private SlackUserProperty resolveSlackPropertyFromEmailViaInferredUsername(String emailAddress){
        String baseUsername = emailAddress.split("@")[0];

        User user = User.get(baseUsername, false, null);
        if(user == null){
            LOGGER.log(Level.INFO, String.format("Could not find user with name '%s' from email '%s'", baseUsername, emailAddress));
            return null;
        }
        String userEmail = resolveUserEmail(user);
        if(userEmail != null && userEmail != emailAddress){
            LOGGER.log(Level.INFO, String.format("User with name '%s' does not match expected email '%s': have '%s'", baseUsername, emailAddress, userEmail));
            return null;
        }
        SlackUserProperty userProperty = user.getProperty(SlackUserProperty.class);
        if(userProperty == null){
            LOGGER.log(Level.INFO, String.format("User with name '%s' does not have slack property", baseUsername));
        }
        LOGGER.fine(String.format("Found Slack ID '%s' for user '%s'", userProperty.getUserId(), baseUsername));
        return userProperty;
    }

    private SlackUserProperty resolveSlackPropertyFromEmailViaUsers(String emailAddress){
        List<User> usersWithEmailAddress = User.getAll().stream()
            .filter(user -> resolveUserEmail(user) == emailAddress)
            .collect(Collectors.toList());
        List<ResolvedUserConfig> usersPerId = usersWithEmailAddress.stream()
            .map(user -> new ResolvedUserConfig(user))
            .filter(user -> user.slackProperty != null && user.slackProperty.getUserId() != null)
            .filter(distinctSlackUserProperties())
            .collect(Collectors.toList());
        if(usersPerId.size() > 1) {
            List<String> conflictingUsersDisplayName = usersPerId.stream()
                .map(resolved -> resolved.user.getDisplayName())
                .collect(Collectors.toList());
            LOGGER.log(Level.WARNING, String.format(
                    "Multiple users found with email '%s' having different slack IDs or configuration: %s", emailAddress, String.join(",", conflictingUsersDisplayName)));
        }
        Optional<ResolvedUserConfig> pickedUser = usersPerId.stream().findFirst();
        if(pickedUser.isPresent()) {
            return pickedUser.get().slackProperty;
        }
        return null;
    }

    private class ResolvedUserConfig {
        private User user;
        private SlackUserProperty slackProperty;
        public ResolvedUserConfig(User user) {
            this.user = user;
            this.slackProperty = user.getProperty(SlackUserProperty.class);
        }
    }

    public static Predicate<ResolvedUserConfig> distinctSlackUserProperties() {
        Set<String> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(String.format("%s-%b", t.slackProperty.getUserId(), t.slackProperty.getDisableNotifications()));
    }

    @Extension
    public static class DescriptorImpl extends SlackUserIdResolverDescriptor {

        @Override
        public String getDisplayName() {
            return "Slack email User ID Resolver";
        }
    }
}
