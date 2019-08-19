package jenkins.plugins.slack;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.model.Item;
import hudson.model.Project;
import hudson.model.Run;
import hudson.security.ACL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import static java.lang.String.format;

public class CredentialsObtainer {

    private static final Logger logger = Logger.getLogger(CredentialsObtainer.class.getName());

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
            throw new IllegalArgumentException(format("the credential with the provided ID (%s) could not be found and no token was specified", credentialId));
        }
        return response;
    }

    /**
     * Tries to obtain the proper Item object to provide to CredentialsProvider.
     * Project works for freestyle jobs, the parent of the Run works for pipelines.
     * In case the proper item cannot be found, null is returned, since when null is provided to CredentialsProvider,
     * it will internally use Jenkins.getInstance() which effectively only allows global credentials.
     *
     * @return the item to use for CredentialsProvider credential lookup
     */
    @Restricted(NoExternalUse.class)
    public static Item getItemForCredentials(StepContext context) {
        Item item = null;
        try {
            item = context.get(Project.class);
            if (item == null) {
                Run run = context.get(Run.class);
                if (run != null) {
                    item = run.getParent();
                }
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "Exception obtaining item for credentials lookup. Only global credentials will be available", e);
        }
        return item;
    }
    
    private static StringCredentials getCredentialWithId(String credentialId, List<StringCredentials> credentials) {
        CredentialsMatcher matcher = CredentialsMatchers.withId(credentialId);
        return CredentialsMatchers.firstOrNull(credentials, matcher);
    }
}
