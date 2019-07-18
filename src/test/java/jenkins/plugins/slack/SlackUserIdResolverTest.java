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

import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
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
    static final String AUTH_TOKEN = "token";

    CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    StatusLine statusLine = mock(StatusLine.class);

    @Before
    public void setupSlackUserIdResolver() throws Exception {
        mockStatic(EntityUtils.class);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(response);
        when(response.getStatusLine()).thenReturn(statusLine);
    }

    @Test
    public void testResolveUserIdForEmailAddress() throws IOException {
        when(statusLine.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        SlackUserIdResolver resolver = SlackUserIdResolver.get(AUTH_TOKEN, httpClient);

        String reponseOkString = IOUtils.toString(
                this.getClass().getResourceAsStream("lookUpByEmailResponseOK.json")
        );
        when(EntityUtils.toString(any(HttpEntity.class))).thenReturn(reponseOkString);
        String userId = resolver.resolveUserIdForEmailAddress(EMAIL_ADDRESS);
        assertEquals(EXPECTED_USER_ID, userId);

        String reponseErrorString = IOUtils.toString(
                this.getClass().getResourceAsStream("lookUpByEmailResponseError.json")
        );
        when(EntityUtils.toString(any(HttpEntity.class))).thenReturn(reponseErrorString);
        userId = resolver.resolveUserIdForEmailAddress(EMAIL_ADDRESS);
        assertEquals(null, userId);
    }
}
