package jenkins.plugins.slack;

public class StandardSlackServiceStub extends StandardSlackService {

    private HttpClientStub httpClientStub;

    public StandardSlackServiceStub(String teamDomain, String token, boolean botUser, String roomId) {
        super(teamDomain, token, botUser, roomId);
    }

    @Override
    public HttpClientStub getHttpClient() {
        return httpClientStub;
    }

    public void setHttpClient(HttpClientStub httpClientStub) {
        this.httpClientStub = httpClientStub;
    }
}
