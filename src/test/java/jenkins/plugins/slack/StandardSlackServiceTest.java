package jenkins.plugins.slack;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

public class StandardSlackServiceTest {
    /**
     * Publish should generally not rethrow exceptions, or it will cause a build job to fail at end.
     */
    @Test
    public void publishWithBadHostShouldNotRethrowExceptions() {
        StandardSlackService service = new StandardSlackService("", "foo", "token", null, false, "#general", "apiToken");
        service.setHost("hostvaluethatwillcausepublishtofail");
        service.publish("message");
    }

    /**
     * Use a valid host, but an invalid team domain
     */
    @Test
    public void invalidTeamDomainShouldFail() {
        StandardSlackService service = new StandardSlackService("", "my", "token", null, false, "#general", "apiToken");
        service.publish("message");
    }

    /**
     * Use a valid team domain, but a bad token
     */
    @Test
    public void invalidTokenShouldFail() {
        StandardSlackService service = new StandardSlackService("", "tinyspeck", "token", null, false, "#general", "apiToken");
        service.publish("message");
    }

    @Test
    public void publishToASingleRoomSendsASingleMessage() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("","domain", "token", null, false, "#room1", "apiToken");
        ClientStub clientStub = new ClientStub();
        service.setClientStub(clientStub);
        service.publish("message");
        assertEquals(1, service.getClient().getNumberOfCallsToExecuteMethod());
    }

    @Test
    public void publishToMultipleRoomsSendsAMessageToEveryRoom() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("","domain", "token", null, false, "#room1,#room2,#room3", "apiToken");
        ClientStub clientStub = new ClientStub();
        service.setClientStub(clientStub);
        service.publish("message");
        assertEquals(3, service.getClient().getNumberOfCallsToExecuteMethod());
    }

    @Test
    public void successfulPublishToASingleRoomReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("","domain", "token", null, false, "#room1", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_OK, ""));
        service.setClientStub(clientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void successfulPublishToMultipleRoomsReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("","domain", "token", null, false, "#room1,#room2,#room3", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_OK, ""));
        service.setClientStub(clientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void failedPublishToASingleRoomReturnsFalse() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", "token", null, false, "#room1", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_NOT_FOUND, ""));
        service.setClientStub(clientStub);
        assertFalse(service.publish("message"));
    }

    @Test
    public void singleFailedPublishToMultipleRoomsReturnsFalse() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", "token", null, false, "#room1,#room2,#room3", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setFailAlternateResponses(true);
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_OK, ""));
        service.setClientStub(clientStub);
        assertFalse(service.publish("message"));
    }

    @Test
    public void publishToEmptyRoomReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", "token", null, false, "", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_OK, ""));
        service.setClientStub(clientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void sendAsBotUserReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", "token", null, true, "#room1", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_OK, ""));
        service.setClientStub(clientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void getUserIdWithEmptyApiTokenReturnsNull() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", "token", null, false, "#room1", "");
        ClientStub clientStub = new ClientStub();
        service.setClientStub(clientStub);
        assertNull(service.getUserId("john.doe@example.com"));
    }

    @Test
    public void getUserIdReturnsUserIdWhenFound() throws IOException {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", "token", null, false, "", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(
                HttpStatus.SC_OK,
                IOUtils.toString(this.getClass().getResourceAsStream("/slack-api-users.list.json"), "UTF-8"))
        );
        service.setClientStub(clientStub);
        assertEquals("U125V2OQL", service.getUserId("john.doe@example.com"));
    }

    @Test
    public void getUserIdReturnsNullWhenNotFound() throws IOException {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", "token", null, false, "", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(
                HttpStatus.SC_OK,
                IOUtils.toString(this.getClass().getResourceAsStream("/slack-api-users.list.json"), "UTF-8"))
        );
        service.setClientStub(clientStub);
        assertNull(service.getUserId("john.doe2@example.com"));
    }

    @Test
    public void getUserIdReturnsNullWhenHttpStatusNotOk() throws IOException {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", "token", null, false, "", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, ""));
        service.setClientStub(clientStub);
        assertNull(service.getUserId("john.doe@example.com"));
    }

    @Test
    public void getUserIdReturnsNullWhenHttpStatusBodyNotOk() throws IOException {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", "token", null, false, "", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(HttpStatus.SC_OK, "{\"ok\":false,\"error\":\"API error\"}"));
        service.setClientStub(clientStub);
        assertNull(service.getUserId("john.doe@example.com"));
    }

    @Test
    public void getUserListReturnsUserList() throws IOException {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", "token", null, false, "", "apiToken");
        ClientStub clientStub = new ClientStub();
        clientStub.setClientResponse(new ClientResponse(
                HttpStatus.SC_OK,
                IOUtils.toString(this.getClass().getResourceAsStream("/slack-api-users.list.json"), "UTF-8"))
        );
        service.setClientStub(clientStub);

        List<SlackUser> userList = service.getUserList();
        assertEquals(1, userList.size());

        SlackUser user = userList.get(0);
        assertEquals("john.doe", user.getName());
        assertFalse(user.isDeleted());
    }

}
