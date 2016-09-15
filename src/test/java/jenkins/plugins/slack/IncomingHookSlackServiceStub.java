package jenkins.plugins.slack;

public class IncomingHookSlackServiceStub extends IncomingHookSlackService {

    private HttpClientStub httpClientStub;

    public IncomingHookSlackServiceStub(String webhookUrl) {
        super(webhookUrl);
    }

    @Override
    public HttpClientStub getHttpClient() {
        return httpClientStub;
    }

    public void setHttpClient(HttpClientStub httpClientStub) {
        this.httpClientStub = httpClientStub;
    }
}
