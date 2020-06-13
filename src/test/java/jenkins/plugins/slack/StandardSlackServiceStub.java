package jenkins.plugins.slack;

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
