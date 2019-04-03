package jenkins.plugins.slack;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Item;
import hudson.security.ACL;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

public class CredentialsObtainer {

    public static StringCredentials lookupCredentials(String credentialId) {
        List<StringCredentials> credentials = CredentialsProvider.lookupCredentials(StringCredentials.class, Jenkins.get(), ACL.SYSTEM, Collections.emptyList());
        return getCredentialWithId(credentialId, credentials);
    }

    public static StringCredentials lookupCredentials(String credentialId, Item item) {
        List<StringCredentials> credentials = CredentialsProvider.lookupCredentials(StringCredentials.class, item, ACL.SYSTEM, Collections.emptyList());
        return getCredentialWithId(credentialId, credentials);
    }

    /**
     * Attempts to obtain the credential with the providedId from the item's credential context, otherwise returns token
     * @param credentialId the id from the credential to be used
     * @param item the item with the context to obtain the credential from.
     * @param token the fallback token
     * @return the obtained token
     */
    public static String getTokenToUse(String credentialId, Item item, String token) {
        String response;
        if (StringUtils.isEmpty(credentialId)) {
            response = token;
        } else {
            StringCredentials credentials = lookupCredentials(StringUtils.trim(credentialId), item);
            if (credentials != null) {
                response = credentials.getSecret().getPlainText();
            } else {
                response = token;
            }
        }
        if (StringUtils.isEmpty(response)) {
            throw new IllegalArgumentException("the token with the provided ID could not be found and no token was specified");
        }
        return response;
    }

    private static StringCredentials getCredentialWithId(String credentialId, List<StringCredentials> credentials) {
        CredentialsMatcher matcher = CredentialsMatchers.withId(credentialId);
        return CredentialsMatchers.firstOrNull(credentials, matcher);
    }
}
