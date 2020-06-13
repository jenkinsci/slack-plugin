package jenkins.plugins.slack;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class SlackApiTLSTest {

    public static final String SLACK_API_TEST = "https://slack.com/api/api.test";

    @Test
    public void connectToAPI() {
        try (CloseableHttpClient httpClient = HttpClient.getCloseableHttpClient(null)) {
            HttpPost post = new HttpPost(SLACK_API_TEST);
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                StatusLine statusLine = response.getStatusLine();
                assertThat(statusLine.getStatusCode(), is(200));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void connectToAPIUsingTLS11() throws NoSuchAlgorithmException {
        final HttpClientBuilder clientBuilder = HttpClients
            .custom()
            .setSSLSocketFactory(new SSLConnectionSocketFactory(SSLContext.getDefault(),
                new String[] { "TLSv1.1" }, null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier()));
        try (CloseableHttpClient httpClient = clientBuilder.build()) {
            HttpPost post = new HttpPost(SLACK_API_TEST);
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            try (CloseableHttpResponse ignored = httpClient.execute(post)) {
                fail("Slack no longer accepts anything below TLSv1.2");
            } catch (SSLException ex) {
                assertThat(ex.getMessage(), is("Received fatal alert: protocol_version"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
