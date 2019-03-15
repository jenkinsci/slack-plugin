package jenkins.plugins.slack;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import hudson.model.Item;
import hudson.security.ACL;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import java.util.Collections;
import java.util.List;

public class CredentialsObtainer {

    public static StringCredentials lookupCredentials(String credentialId, Item item) {
        List<StringCredentials> credentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(StringCredentials.class, item, ACL.SYSTEM, Collections.emptyList());
        CredentialsMatcher matcher = CredentialsMatchers.withId(credentialId);
        return CredentialsMatchers.firstOrNull(credentials, matcher);
    }

    /**
     * Attempts to obtain the credential with the providedId from the item's credential context, otherwise returns token
     * @param credentialId the id from the credential to be used
     * @param item the item with the context to obtain the credential from.
     * @param token the fallback token
     * @return the obtained token
     */
    public static  String getTokenToUse(String credentialId, Item item, String token) {
        if (StringUtils.isEmpty(credentialId)) {
            return token;
        }
        StringCredentials credentials = lookupCredentials(StringUtils.trim(credentialId), item);
        final String response;
        if (credentials != null) {
            response = credentials.getSecret().getPlainText();
        } else {
            response = token;
        }
        return response;
    }
}
