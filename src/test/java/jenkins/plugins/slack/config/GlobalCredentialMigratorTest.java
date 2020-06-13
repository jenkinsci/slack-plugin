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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings("deprecation")
public class GlobalCredentialMigratorTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private SlackNotifier.DescriptorImpl descriptor;

    @Before
    public void setup() {
        descriptor = ExtensionList.lookupSingleton(SlackNotifier.DescriptorImpl.class);
    }

    @Test
    @LocalData
    public void noToken() {
        assertNull(Util.fixEmpty(descriptor.getToken()));
        assertEquals(descriptor.getTokenCredentialId(), "abcdef");
    }

    @Test
    @LocalData
    public void withToken() {
        assertNull(Util.fixEmpty(descriptor.getToken()));
        String tokenCredentialId = descriptor.getTokenCredentialId();

        StringCredentials stringCredentials = lookupCredentials(tokenCredentialId);
        assertEquals(stringCredentials.getDescription(), Messages.migratedCredentialDescription());
    }

    private StringCredentials lookupCredentials(String credentialId) {
        List<StringCredentials> credentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(StringCredentials.class, Jenkins.getInstance(), ACL.SYSTEM, Collections
                .emptyList());
        CredentialsMatcher matcher = CredentialsMatchers.withId(credentialId);
        return CredentialsMatchers.firstOrNull(credentials, matcher);
    }
}
