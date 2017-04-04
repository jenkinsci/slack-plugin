package jenkins.plugins.slack;

public class StandardSlackServiceStub extends StandardSlackService {

    private ClientStub clientStub;

    public StandardSlackServiceStub(String baseUrl, String teamDomain, String token, String tokenCredentialId, boolean botUser, String roomId, String apiToken) {
        super(baseUrl, teamDomain, token, tokenCredentialId, botUser, roomId, apiToken);
    }

    @Override
    protected ClientStub getClient() {
        return this.clientStub;
    }

    public void setClientStub(ClientStub client) {
        this.clientStub = client;
    }
}
