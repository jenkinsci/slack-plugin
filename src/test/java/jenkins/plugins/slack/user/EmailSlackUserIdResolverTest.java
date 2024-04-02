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
package jenkins.plugins.slack.user;

import hudson.model.Run;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.tasks.MailAddressResolver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jenkins.plugins.slack.CloseableHttpClientStub;
import jenkins.plugins.slack.CloseableHttpResponseStub;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.StringEntity;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.FakeChangeLogSCM.EntryImpl;
import org.jvnet.hudson.test.FakeChangeLogSCM.FakeChangeLogSet;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    }

    @Test
    public void testResolveUserIdForEmailAddressViaSlack() throws IOException {
        String userId;

        httpClient.setHttpResponse(getResponseOK());
        userId = resolver.resolveUserIdForEmailAddress(EMAIL_ADDRESS);
        assertEquals(EXPECTED_USER_ID, userId);
    }

    @Test
    public void testResolveUserIdFindByInferredUsername() throws IOException {
        String userId;
        httpClient.setHttpResponse(getResponseError());
        User mockUser = mock(User.class);
        SlackUserProperty userProperty = new SlackUserProperty();
        userProperty.setUserId(EXPECTED_USER_ID);
        when(mockUser.getProperty(SlackUserProperty.class)).thenReturn(userProperty);
        try (MockedStatic<User> userMock = Mockito.mockStatic(User.class)) {
            userMock.when(() -> User.get(anyString(), anyBoolean(), eq(null))).thenReturn(mockUser);

            userId = resolver.resolveUserIdForEmailAddress(EMAIL_ADDRESS);

            assertEquals(EXPECTED_USER_ID, userId);
            verify(mailAddressResolver, times(1)).findMailAddressFor(mockUser);
            userMock.verify(() -> User.get(eq("spengler"), eq(false), any()), times(1));
        }
    }

    @Test
    public void testResolveUserIdFindByInferredUsernameBadEmail() throws IOException {
        String userId;
        httpClient.setHttpResponse(getResponseError());
        User mockUser = mock(User.class);
        SlackUserProperty userProperty = new SlackUserProperty();
        userProperty.setUserId(EXPECTED_USER_ID);
        when(mockUser.getProperty(SlackUserProperty.class)).thenReturn(userProperty);
        when(mailAddressResolver.findMailAddressFor(any(User.class))).thenReturn("nope@example.com");
        try (MockedStatic<User> userMock = Mockito.mockStatic(User.class)) {
            userMock.when(() -> User.get(anyString(), anyBoolean(), eq(null))).thenReturn(mockUser);

            userId = resolver.resolveUserIdForEmailAddress(EMAIL_ADDRESS);

            assertNull(userId);
            verify(mailAddressResolver, times(1)).findMailAddressFor(mockUser);
            userMock.verify(() -> User.get(eq("spengler"), eq(false), any()), times(1));
        }
    }

    @Test
    public void testResolveUserIdFindByUsersListing() throws IOException {
        String userId;
        httpClient.setHttpResponse(getResponseError());
        User mockUser = mock(User.class);
        SlackUserProperty userProperty = new SlackUserProperty();
        userProperty.setUserId(EXPECTED_USER_ID);
        when(mockUser.getProperty(SlackUserProperty.class)).thenReturn(userProperty);
        try (MockedStatic<User> userMock = Mockito.mockStatic(User.class)) {
            userMock.when(() -> User.get(anyString(), anyBoolean(), eq(null))).thenReturn(null);
            userMock.when(() -> User.getAll()).thenReturn(Collections.singletonList(mockUser));

            userId = resolver.resolveUserIdForEmailAddress(EMAIL_ADDRESS);

            assertEquals(EXPECTED_USER_ID, userId);
            verify(mailAddressResolver, times(1)).findMailAddressFor(mockUser);
            userMock.verify(() -> User.get(eq("spengler"), eq(false), any()), times(1));
        }
    }

    @Test
    public void testResolveUserIdForUserWithoutResolvableEmailAddress() throws Exception {
        mailAddressResolver = mock(MailAddressResolver.class);
        resolver = new EmailSlackUserIdResolver(AUTH_TOKEN, httpClient, Collections.emptyList(), user -> null);
        httpClient.setHttpResponse(getResponseOK());

        SlackUserProperty userProperty = resolver.fetchUserSlackProperty(mock(User.class));
        assertNull(userProperty);
    }

    @Test
    public void testResolveUserIdForUserWithResolvableEmailAddressViaResolver() throws Exception {
        resolver = new EmailSlackUserIdResolver(AUTH_TOKEN, httpClient, Collections.singletonList(mailAddressResolver), user -> null);
        httpClient.setHttpResponse(getResponseOK());

        SlackUserProperty userProperty = resolver.fetchUserSlackProperty(mock(User.class));
        assertNotNull(userProperty);
        assertEquals(EXPECTED_USER_ID, userProperty.getUserId());
        assertEquals(false, userProperty.getDisableNotifications());
    }

    @Test
    public void testResolveUserIdWithoutAuthToken() throws Exception {
        mailAddressResolver = mock(MailAddressResolver.class);
        resolver = new EmailSlackUserIdResolver(AUTH_TOKEN, httpClient, Collections.singletonList(mailAddressResolver), user -> EMAIL_ADDRESS);
        resolver.setAuthToken(null);
        httpClient.setHttpResponse(getResponseOK());

        SlackUserProperty userProperty = resolver.fetchUserSlackProperty(mock(User.class));
        assertNull(userProperty);
    }

    @Test
    public void testResolveUserIdForUserWithoutDefaultMailAddressResolver() throws Exception {
        mailAddressResolver = mock(MailAddressResolver.class);
        resolver = new EmailSlackUserIdResolver(AUTH_TOKEN, httpClient, Collections.singletonList(mailAddressResolver), null);
        httpClient.setHttpResponse(getResponseOK());

        SlackUserProperty userProperty = resolver.fetchUserSlackProperty(mock(User.class));
        assertNull(userProperty);
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

        resolver = new EmailSlackUserIdResolver(AUTH_TOKEN, httpClient, null, user -> EMAIL_ADDRESS);
        httpClient.setHttpResponse(getResponseOK());

        String userId = resolver.findOrResolveUserId(mock(User.class));

        assertEquals(EXPECTED_USER_ID, userId);
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
