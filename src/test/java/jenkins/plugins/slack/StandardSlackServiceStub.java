package jenkins.plugins.slack;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClientStub;

public class StandardSlackServiceStub extends StandardSlackService {

    private CloseableHttpClientStub httpClientStub;

    public StandardSlackServiceStub(StandardSlackServiceBuilder standardSlackServiceBuilder) {
        super(standardSlackServiceBuilder);
    }

    @Override
    public CloseableHttpClientStub getHttpClient() {
        return httpClientStub;
    }

    public void setHttpClient(CloseableHttpClientStub httpClientStub) {
        this.httpClientStub = httpClientStub;
    }
}
