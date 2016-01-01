package jenkins.plugins.slack;

public class StandardSlackServiceStub extends StandardSlackService {

    private CloseableHttpClientStub closeableHttpClientStub;

    public StandardSlackServiceStub(String teamDomain, String token, String roomId) {
        super(teamDomain, token, roomId);
    }

    @Override
    public CloseableHttpClientStub getHttpClient() {
        return closeableHttpClientStub;
    }

    public void setHttpClient(CloseableHttpClientStub closeableHttpClientStub) {
        this.closeableHttpClientStub = closeableHttpClientStub;
    }
}
