package jenkins.plugins.slack;

public class StandardSlackServiceStub extends StandardSlackService {

    private HttpClientStub httpClientStub;

    public StandardSlackServiceStub(String baseUrl, String teamDomain, String token, String tokenCredentialId, boolean botUser, String roomId) {
        super(baseUrl, teamDomain, token, tokenCredentialId, botUser, roomId);
    }

    @Override
    public HttpClientStub getHttpClient() {
        return httpClientStub;
    }

    public void setHttpClient(HttpClientStub httpClientStub) {
        this.httpClientStub = httpClientStub;
    }
}
