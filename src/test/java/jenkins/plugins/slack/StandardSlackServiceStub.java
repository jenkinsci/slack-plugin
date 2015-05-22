package jenkins.plugins.slack;

public class StandardSlackServiceStub extends StandardSlackService {

    private HttpClientStub httpClientStub;

    public StandardSlackServiceStub(String teamDomain, String token, String roomId) {
        super(teamDomain, token, roomId);
    }

    @Override
    public HttpClientStub getHttpClient() {
        return httpClientStub;
    }

    public void setHttpClient(HttpClientStub httpClientStub) {
        this.httpClientStub = httpClientStub;
    }
}
