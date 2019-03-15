package jenkins.plugins.slack;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.ExtensionList;
import hudson.security.ACL;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.http.HttpStatus;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, StandardSlackService.class, CredentialsProvider.class, Secret.class})
public class StandardSlackServiceCredentialsTest {
    @Mock
    Jenkins jenkins;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Jenkins.class);
    }

    @Test
    public void populatedTokenIsUsed() {
        final String populatedToken = "secret-text";
        final StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", true, "#room1:1528317530", populatedToken);
        final CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertTrue(httpClientStub.getLastRequest().getURI().toString().contains(populatedToken));
    }

    @Test
    public void globalCredentialByIdUsed() {
        final String id = "cred-2id";
        final String secretText = "secret-2text";
        setupGlobalAvailableCredentialId(id, secretText);
        final StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", null, id, true, "#room1:1528317530");
        final CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertTrue(httpClientStub.getLastRequest().getURI().toString().contains(secretText));
    }

    @Test
    public void tokenIsUsed() {
        final String token = "explicittoken";
        final StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", token, null, true, "#room1:1528317530");
        final CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertTrue(httpClientStub.getLastRequest().getURI().toString().contains(token));
    }

    private void setupGlobalAvailableCredentialId(final String id, final String secretText) {
        final CredentialsProvider credentialsProvider = mock(CredentialsProvider.class);
        final ExtensionList<CredentialsProvider> extensionList = mock(ExtensionList.class);
        final List<CredentialsProvider> listProviders = new ArrayList<>(1);
        final List<StringCredentials> credentialList = new ArrayList<>();
        final StringCredentials credentials = mock(StringCredentials.class);
        final Secret secret = mock(Secret.class);
        listProviders.add(credentialsProvider);
        when(extensionList.iterator()).thenReturn(listProviders.iterator());
        when(Jenkins.getInstance()).thenReturn(jenkins);
        when(jenkins.getExtensionList(CredentialsProvider.class)).thenReturn(extensionList);
        when(credentials.getId()).thenReturn(id);
        when(secret.getPlainText()).thenReturn(secretText);
        when(credentials.getSecret()).thenReturn(secret);
        credentialList.add(credentials);
        when(credentialsProvider.getCredentials(Matchers.eq(StringCredentials.class), Matchers.eq(jenkins), Matchers.eq(ACL.SYSTEM), Matchers.eq(Collections.emptyList()))).thenReturn(credentialList);
    }

}
