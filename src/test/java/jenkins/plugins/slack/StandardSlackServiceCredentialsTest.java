package jenkins.plugins.slack;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.ExtensionList;
import hudson.model.*;
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

import static org.junit.Assert.assertFalse;
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
    public void providedJobCredentialsAreUsed() {
        final String id = "cred-id";
        final String secretText = "secret-text";
        final Job job = createJobWithAvailableCredentialId(id, secretText);
        final StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", null, id, true, "#room1:1528317530", job);
        final CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertTrue(httpClientStub.getLastRequest().getURI().toString().contains(secretText));
    }

    @Test
    public void globalCredentialsCanBeUsed() {
        final String id = "cred-2id";
        final String secretText = "secret-2text";
        setupGlobalAvailableCredentialId(id, secretText);
        final StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", null, id, true, "#room1:1528317530", null);
        final CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertTrue(httpClientStub.getLastRequest().getURI().toString().contains(secretText));
    }

    @Test
    public void otherJobCredentialsAreNotObtained() {
        final String id = "cred-id";
        final String secretText = "secret-text";
        final Job job = createJobWithAvailableCredentialId(id, secretText);
        final StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", null, "wrongid", true, "#room1:1528317530", job);
        final CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertFalse(httpClientStub.getLastRequest().getURI().toString().contains(secretText));
    }

    @Test
    public void ifNoJobProvidedTokenIsUsed() {
        final String token = "explicittoken";
        final StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", token, null, true, "#room1:1528317530", null);
        final CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertTrue(httpClientStub.getLastRequest().getURI().toString().contains(token));
    }

    /**
     * Creates a mocked job and sets up the mocking mechanism so the provided credential is obtainable.
     * @param id the id of the credential to use.
     * @param secretText the secret text of the credential.
     * @return a mocked job set up so it can use the provided credentials
     */
    private Job createJobWithAvailableCredentialId(final String id, final String secretText) {
        final Job job = mock(Job.class);
        final CredentialsProvider credentialsProvider = mock(CredentialsProvider.class);
        final List<StringCredentials> credentialList = setupMocks(id, secretText, credentialsProvider);
        when(credentialsProvider.getCredentials(Matchers.eq(StringCredentials.class), Matchers.eq(job), Matchers.eq(ACL.SYSTEM), Matchers.eq(Collections.emptyList()))).thenReturn(credentialList);
        return job;
    }

    private void setupGlobalAvailableCredentialId(final String id, final String secretText) {
        final CredentialsProvider credentialsProvider = mock(CredentialsProvider.class);
        final List<StringCredentials> credentialList = setupMocks(id, secretText, credentialsProvider);
        when(credentialsProvider.getCredentials(Matchers.eq(StringCredentials.class), Matchers.eq(jenkins), Matchers.eq(ACL.SYSTEM), Matchers.eq(Collections.emptyList()))).thenReturn(credentialList);
    }

    private List<StringCredentials> setupMocks(String id, String secretText, CredentialsProvider credentialsProvider) {
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
        return credentialList;
    }

}
