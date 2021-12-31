package jenkins.plugins.slack.config;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.security.ACL;
import hudson.util.Secret;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.Messages;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;

@SuppressWarnings("deprecation")
public class GlobalCredentialMigrator {

    private static final Logger LOGGER = Logger.getLogger(GlobalCredentialMigrator.class
            .getName());

    public StandardCredentials migrate(@NonNull String token) {
        LOGGER.info("Migrating slack global config: moving integration token text into a credential");

        List<StringCredentials> allStringCredentials =
                CredentialsMatchers.filter(
                        CredentialsProvider.lookupCredentials(
                                StringCredentials.class,
                                Jenkins.getInstance(),
                                ACL.SYSTEM,
                                (DomainRequirement) null),
                        CredentialsMatchers.always()
                );

        return allStringCredentials
                .stream()
                .filter(cred -> cred.getSecret().getPlainText().equals(token))
                .findAny()
                .orElseGet(() -> addCredentialIfNotPresent(token));
    }

    private StringCredentials addCredentialIfNotPresent(@NonNull String token) {
        StringCredentials credentials = new StringCredentialsImpl(
                CredentialsScope.GLOBAL,
                UUID.randomUUID().toString(),
                Messages.migratedCredentialDescription(),
                Secret.fromString(token)
        );

        SystemCredentialsProvider instance = SystemCredentialsProvider.getInstance();
        instance.getCredentials().add(credentials);

        try {
            instance.save();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return credentials;
    }
}
