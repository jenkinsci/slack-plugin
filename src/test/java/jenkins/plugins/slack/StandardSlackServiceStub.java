package jenkins.plugins.slack;

public class StandardSlackServiceStub extends StandardSlackService {

    private CloseableHttpClientStub httpClientStub;

    public StandardSlackServiceStub(String baseUrl, String teamDomain, boolean botUser, String roomId, String populatedToken) {
        super(baseUrl, teamDomain, botUser, roomId, false, populatedToken);
    }

    @Override
    public CloseableHttpClientStub getHttpClient() {
        return httpClientStub;
    }

    public void setHttpClient(CloseableHttpClientStub httpClientStub) {
        this.httpClientStub = httpClientStub;
    }
}
