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
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.FakeChangeLogSCM.EntryImpl;
import org.jvnet.hudson.test.FakeChangeLogSCM.FakeChangeLogSet;
import org.jvnet.hudson.test.JenkinsRule;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 *
 * @author stuartr
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.net.ssl.*"})
@PrepareForTest(EntityUtils.class)
public class SlackUserIdResolverTest {

    static final String EXPECTED_USER_ID = "W012A3CDE";
    static final String EMAIL_ADDRESS = "spengler@ghostbusters.example.com";
    static final String USERNAME = "spengler";
    static final String AUTH_TOKEN = "token";

    @Rule
    JenkinsRule jenkinsRule = new JenkinsRule();

    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);
    SlackUserIdResolver resolver;

    @Before
    public void setupSlackUserIdResolver() throws Exception {
        mockStatic(EntityUtils.class);

        // Set up the default HTTP response
        String reponseOkString = IOUtils.toString(
                this.getClass().getResourceAsStream("lookUpByEmailResponseOK.json")
        );
        when(EntityUtils.toString(any(HttpEntity.class))).thenReturn(reponseOkString);
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getStatusLine()).thenReturn(statusLine);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);

        // Setup MailAddressResolver mock
        MailAddressResolver mailAddressResolver = mock(MailAddressResolver.class);
        when(mailAddressResolver.findMailAddressFor(any(User.class))).thenReturn(EMAIL_ADDRESS);
        List<MailAddressResolver> mailAddressResolverList = new ArrayList<>();
        mailAddressResolverList.add(mailAddressResolver);

        // Create a reesolver for use by tests
        resolver = SlackUserIdResolver.get(AUTH_TOKEN, httpClient, mailAddressResolverList);
    }

    @Test
    public void testResolveUserIdForEmailAddress() throws IOException {
        String userId = resolver.resolveUserIdForEmailAddress(EMAIL_ADDRESS);
        assertEquals(EXPECTED_USER_ID, userId);

        // Test handling of an error response from Slack
        String reponseErrorString = IOUtils.toString(
                this.getClass().getResourceAsStream("lookUpByEmailResponseError.json")
        );
        when(EntityUtils.toString(any(HttpEntity.class))).thenReturn(reponseErrorString);
        userId = resolver.resolveUserIdForEmailAddress(EMAIL_ADDRESS);
        assertEquals(null, userId);
    }

    @Test
    public void testResolveUserIdForUser() throws Exception {
        // MailAddressResolver is mocked to return EMAIL_ADDRESS associated with
        // the EXPECTED_USER_ID
        String userId = resolver.resolveUserId(mock(User.class));
        assertEquals(EXPECTED_USER_ID, userId);
    }

    @Test
    public void testResolveUserIdForChangelogSet() throws Exception {
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
}
