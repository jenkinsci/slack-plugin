package jenkins.plugins.slack.config;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import hudson.ExtensionList;
import hudson.Util;
import hudson.security.ACL;
import java.util.Collections;
import java.util.List;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.Messages;
import jenkins.plugins.slack.SlackNotifier;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.jvnet.hudson.test.recipes.LocalData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SuppressWarnings("deprecation")
@WithJenkins
class GlobalCredentialMigratorTest {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private JenkinsRule j;
    private SlackNotifier.DescriptorImpl descriptor;

    @BeforeEach
    void setup(JenkinsRule j) {
        this.j = j;
        descriptor = ExtensionList.lookupSingleton(SlackNotifier.DescriptorImpl.class);
    }

    @Test
    @LocalData
    void noToken() {
        assertNull(Util.fixEmpty(descriptor.getToken()));
        assertEquals("abcdef", descriptor.getTokenCredentialId());
    }

    @Test
    @LocalData
    void withToken() {
        assertNull(Util.fixEmpty(descriptor.getToken()));
        String tokenCredentialId = descriptor.getTokenCredentialId();

        StringCredentials stringCredentials = lookupCredentials(tokenCredentialId);
        assertEquals(Messages.migratedCredentialDescription(), stringCredentials.getDescription());
    }

    private StringCredentials lookupCredentials(String credentialId) {
        List<StringCredentials> credentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(StringCredentials.class, Jenkins.getInstance(), ACL.SYSTEM, Collections
                .emptyList());
        CredentialsMatcher matcher = CredentialsMatchers.withId(credentialId);
        return CredentialsMatchers.firstOrNull(credentials, matcher);
    }
}
