package jenkins.plugins.slack;

import org.apache.http.HttpStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StandardSlackServiceTest {

    /**
     * Publish should generally not rethrow exceptions, or it will cause a build job to fail at end.
     */
    @Test
    public void publishWithBadHostShouldNotRethrowExceptions() {
        StandardSlackService service = new StandardSlackService("foo", "token", "#general", "apiToken");
        service.setHost("hostvaluethatwillcausepublishtofail");
        service.publish("message");
    }

    /**
     * Use a valid host, but an invalid team domain
     */
    @Test
    public void invalidTeamDomainShouldFail() {
        StandardSlackService service = new StandardSlackService("my", "token", "#general", "apiToken");
        service.publish("message");
    }

    /**
     * Use a valid team domain, but a bad token
     */
    @Test
    public void invalidTokenShouldFail() {
        StandardSlackService service = new StandardSlackService("tinyspeck", "token", "#general", "apiToken");
        service.publish("message");
    }

    @Test
    public void publishToASingleRoomSendsASingleMessage() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "#room1", "apiToken");
        ClientStub clientStub = new ClientStub();
        service.setClientStub(clientStub);
        service.publish("message");
        assertEquals(1, service.getClient().getNumberOfCallsToExecuteMethod());
    }

    @Test
    public void publishToMultipleRoomsSendsAMessageToEveryRoom() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "#room1,#room2,#room3", "apiToken");
        ClientStub clientStub = new ClientStub();
        service.setClientStub(clientStub);
        service.publish("message");
        assertEquals(3, service.getClient().getNumberOfCallsToExecuteMethod());
    }

    @Test
    public void successfulPublishToASingleRoomReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "#room1", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_OK, ""));
        service.setClientStub(clientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void successfulPublishToMultipleRoomsReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "#room1,#room2,#room3", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_OK, ""));
        service.setClientStub(clientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void failedPublishToASingleRoomReturnsFalse() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "#room1", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_NOT_FOUND, ""));
        service.setClientStub(clientStub);
        assertFalse(service.publish("message"));
    }

    @Test
    public void singleFailedPublishToMultipleRoomsReturnsFalse() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "#room1,#room2,#room3", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setFailAlternateResponses(true);
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_OK, ""));
        service.setClientStub(clientStub);
        assertFalse(service.publish("message"));
    }

    @Test
    public void publishToEmptyRoomReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_OK, ""));
        service.setClientStub(clientStub);
        assertTrue(service.publish("message"));
    }
}
