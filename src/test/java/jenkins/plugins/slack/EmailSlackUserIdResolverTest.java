/*
 * The MIT License
 *
 * Copyright (C) 2019 Electronic Arts Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.plugins.slack;

import hudson.model.Run;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.tasks.MailAddressResolver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import jenkins.plugins.slack.user.EmailSlackUserIdResolver;
import jenkins.plugins.slack.user.SlackUserProperty;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.FakeChangeLogSCM.EntryImpl;
import org.jvnet.hudson.test.FakeChangeLogSCM.FakeChangeLogSet;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MailAddressResolver.class})
public class EmailSlackUserIdResolverTest {

    private static final String EXPECTED_USER_ID = "W012A3CDE";
    private static final String EMAIL_ADDRESS = "spengler@ghostbusters.example.com";
    private static final String AUTH_TOKEN = "token";

    private static String responseOKContent;
    private static String responseErrorContent;

    private CloseableHttpClientStub httpClient;
    private EmailSlackUserIdResolver resolver;
    private MailAddressResolver mailAddressResolver;

    public EmailSlackUserIdResolverTest() throws IOException {
        responseOKContent = readResource("lookUpByEmailResponseOK.json");
        responseErrorContent = readResource("lookUpByEmailResponseError.json");
    }

    @Before
    public void setUp() {
        httpClient = new CloseableHttpClientStub();
        mailAddressResolver = getMailAddressResolver();
        resolver = getResolver(mailAddressResolver);
        PowerMockito.mockStatic(MailAddressResolver.class);
    }

    @Test
    public void testResolveUserIdForEmailAddress() throws IOException {
        String userId;

        // Test handling of a success response from Slack
        httpClient.setHttpResponse(getResponseOK());
        userId = resolver.resolveUserIdForEmailAddress(EMAIL_ADDRESS);
        assertEquals(EXPECTED_USER_ID, userId);

        // Test handling of an error response from Slack
        httpClient.setHttpResponse(getResponseError());
        userId = resolver.resolveUserIdForEmailAddress(EMAIL_ADDRESS);
        assertNull(userId);
    }

    @Test
    public void testResolveUserIdForUser() throws Exception {
        // MailAddressResolver is mocked to return EMAIL_ADDRESS associated with
        // the EXPECTED_USER_ID
        httpClient.setHttpResponse(getResponseOK());
        String userId = resolver.findOrResolveUserId(mock(User.class));
        assertEquals(EXPECTED_USER_ID, userId);
    }

    @Test
    public void testResolveUserIdForUserWithoutResolver() throws Exception {

        PowerMockito.when(MailAddressResolver.resolve(any(User.class))).thenReturn(EMAIL_ADDRESS);

        resolver.setMailAddressResolvers(null);
        httpClient.setHttpResponse(getResponseOK());

        String userId = resolver.findOrResolveUserId(mock(User.class));

        assertEquals(EXPECTED_USER_ID, userId);
        PowerMockito.verifyStatic(MailAddressResolver.class, times(1));
        MailAddressResolver.resolve(any(User.class));

    }

    @Test
    public void testResolveUserIdForUserWithSlackUserProperty() {
        SlackUserProperty userProperty = new SlackUserProperty();
        userProperty.setUserId(EXPECTED_USER_ID);
        User mockUser = mock(User.class);
        when(mockUser.getProperty(SlackUserProperty.class)).thenReturn(userProperty);
        String userId = resolver.findOrResolveUserId(mockUser);
        assertEquals(EXPECTED_USER_ID, userId);
        verify(mailAddressResolver, never()).findMailAddressFor(any(User.class));
    }

    @Test
    public void testResolveUserIdForChangelogSet() throws Exception {
        httpClient.setHttpResponse(getResponseOK());

        // Create a FakeChangeLogSet with a single Entry by a mocked User
        EntryImpl mockEntry = mock(EntryImpl.class);
        when(mockEntry.getAuthor()).thenReturn(mock(User.class));
        List<EntryImpl> entries = new ArrayList<>();
        entries.add(mockEntry);
        FakeChangeLogSet changeLogSet = new FakeChangeLogSet(mock(Run.class), entries);

        List<String> userIdList = resolver.resolveUserIdsForChangeLogSet(changeLogSet);
        assertTrue(userIdList.contains(EXPECTED_USER_ID));

        // Create a List of two ChangeLogSets
        List<ChangeLogSet> changeLogSetList = new ArrayList<>();
        changeLogSetList.add(changeLogSet);
        changeLogSetList.add(changeLogSet);

        userIdList = resolver.resolveUserIdsForChangeLogSets(changeLogSetList);
        assertEquals(2, userIdList.size());
        assertTrue(userIdList.contains(EXPECTED_USER_ID));

        List<String> expectedUserIdList = new ArrayList<>();
        expectedUserIdList.add(EXPECTED_USER_ID);
        expectedUserIdList.add(EXPECTED_USER_ID);

        assertTrue(userIdList.containsAll(expectedUserIdList));
    }

    private String readResource(String resourceName) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream(resourceName), StandardCharsets.UTF_8);
    }

    private CloseableHttpResponse getResponse(String content) throws IOException {
        CloseableHttpResponseStub httpResponse = new CloseableHttpResponseStub(HttpStatus.SC_OK);
        httpResponse.setEntity(new StringEntity(content));
        return httpResponse;
    }

    private CloseableHttpResponse getResponseOK() throws IOException {
        return getResponse(responseOKContent);
    }

    private CloseableHttpResponse getResponseError() throws IOException {
        return getResponse(responseErrorContent);
    }

    private MailAddressResolver getMailAddressResolver() {
        mailAddressResolver = mock(MailAddressResolver.class);
        when(mailAddressResolver.findMailAddressFor(any(User.class))).thenReturn(EMAIL_ADDRESS);
        return mailAddressResolver;
    }

    private EmailSlackUserIdResolver getResolver(MailAddressResolver mailAddressResolver) {
        List<MailAddressResolver> mailAddressResolverList = new ArrayList<>();
        mailAddressResolverList.add(mailAddressResolver);
        return new EmailSlackUserIdResolver(AUTH_TOKEN, httpClient, mailAddressResolverList);
    }

}
