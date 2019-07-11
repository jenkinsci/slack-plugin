/*
// TODO: EA License
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
import jenkins.scm.RunWithSCM;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author stuartr
 */
public class SlackUserIdResolver {
    
    private static final Logger LOGGER = Logger.getLogger(SlackUserIdResolver.class.getName());

    private static final String LOOKUP_BY_EMAIL_METHOD_URL_FORMAT = "https://slack.com/api/users.lookupByEmail?token=%s&email=%s";
    private static final String SLACK_OK_FIELD = "ok";
    private static final String SLACK_USER_FIELD = "user";
    private static final String SLACK_ID_FIELD = "is";

    public static List<String> resolveUserIdsForBuild(AbstractBuild build, String authToken) {
        return resolveUserIdsForChangeLogSets(build.getChangeSets(), authToken);
    }

    public static List<String> resolveUserIdsForRun(Run run, String authToken) {
        if (run instanceof RunWithSCM) {
            RunWithSCM r = (RunWithSCM) run;
            return resolveUserIdsForChangeLogSets(r.getChangeSets(), authToken);
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    public static List<String> resolveUserIdsForChangeLogSet(ChangeLogSet changeLogSet, String authToken) {
        return Arrays.stream(changeLogSet.getItems())
                .map(entry -> resolveUserId(((Entry) entry).getAuthor(), authToken))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static List<String> resolveUserIdsForChangeLogSets(List<ChangeLogSet> changeLogSets, String authToken) {
        return changeLogSets.stream()
                .map(changeLogSet -> resolveUserIdsForChangeLogSet(changeLogSet, authToken))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        
    }

    public static List<String> resolveUserIdsForEmailAddresses(List<String> emailAddresses, String authToken) {
        return emailAddresses.stream()
                .map(address -> resolveUserIdForEmailAddress(address,authToken))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static String resolveUserIdForEmailAddress(String emailAddress, String authToken) {
        if (StringUtils.isEmpty(emailAddress) || StringUtils.isEmpty(authToken)) {
            return null;
        }

        // prepare get method for looking up Slack userId by email address
        String url = String.format(LOOKUP_BY_EMAIL_METHOD_URL_FORMAT, authToken, emailAddress);
        try (CloseableHttpClient client = HttpClients.createMinimal()) {
            CloseableHttpResponse response = client.execute(new HttpGet(url));
            int responseCode = response.getStatusLine().getStatusCode();
            // inspect the response content if a 200 response code is received
            if (HttpStatus.SC_OK == responseCode) {
                HttpEntity entity = response.getEntity();
                JSONObject slackResponse = new JSONObject(EntityUtils.toString(entity));
                // additionally, make sure the JSON response contains an 'ok: true' entry
                if (slackResponse.getBoolean(SLACK_OK_FIELD)) {
                    JSONObject slackUser = slackResponse.getJSONObject(SLACK_USER_FIELD);
                    return slackUser.getString(SLACK_ID_FIELD);
                }
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Error getting userId from Slack", ex);
        }
        return null;
    }

    public static String resolveUserId(User user, String authToken) {
        for (MailAddressResolver resolver : MailAddressResolver.LIST) {
            String emailAddress = resolver.findMailAddressFor(user);
            if (StringUtils.isNotEmpty(emailAddress)) {
                String userId = resolveUserIdForEmailAddress(emailAddress, authToken);
                if (StringUtils.isNotEmpty(userId)) {
                    return userId;
                }
            }
        }
        return null;
    }
}
