package jenkins.plugins.slack;

import java.io.IOException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SlackApiTLSTest {

    private static final String SLACK_API_TEST = "https://slack.com/api/api.test";

    @Test
    void connectToAPI() {
        try (CloseableHttpClient httpClient = HttpClient.getCloseableHttpClient(null)) {
            HttpPost post = new HttpPost(SLACK_API_TEST);
            post.setHeader("Content-Type", "application/json; charset=utf-8");
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                assertThat(response.getCode(), is(200));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
