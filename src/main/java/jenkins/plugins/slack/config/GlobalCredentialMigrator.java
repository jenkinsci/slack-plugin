package jenkins.plugins.slack.config;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.Util;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.Messages;
import jenkins.plugins.slack.SlackNotifier;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;
import javax.annotation.CheckForNull;

@SuppressWarnings("deprecation")
public class GlobalCredentialMigrator {

    private static final Logger LOGGER = Logger.getLogger(GlobalCredentialMigrator.class
            .getName());


    @CheckForNull
    public StandardCredentials migrate(SlackNotifier.DescriptorImpl descriptor) throws IOException {
        String token = descriptor.getToken();
        if (Util.fixEmpty(token) != null) {
            LOGGER.info("Migrating slack global config: moving integration token text into a credential");

            StandardCredentials credentials = new StringCredentialsImpl(
                    CredentialsScope.SYSTEM,
                    UUID.randomUUID().toString(),
                    Messages.MigratedCredentialDescription(),
                    Secret.fromString(token)
            );

            Domain global = Domain.global();

            CredentialsStore credentialsStore = StreamSupport
                    .stream(CredentialsProvider.lookupStores(Jenkins.getInstance())
                            .spliterator(), false)
                    .filter(store -> store instanceof SystemCredentialsProvider.StoreImpl)
                    .findFirst()
                    .orElseThrow(RuntimeException::new);

            credentialsStore.addCredentials(global, credentials);
            return credentials;
        }

        LOGGER.fine("No slack integration token found, skipping migration");

        return null;
    }
}
