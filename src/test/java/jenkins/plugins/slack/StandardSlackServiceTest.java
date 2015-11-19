package jenkins.plugins.slack;

import org.apache.http.HttpStatus;
import org.junit.Test;

import static org.junit.Assert.*;

public class StandardSlackServiceTest {

    /**
     * Publish should generally not rethrow exceptions, or it will cause a build job to fail at end.
     */
    @Test
    public void publishWithBadHostShouldNotRethrowExceptions() {
        StandardSlackService service = new StandardSlackService("foo", "token", "#general");
        service.setHost("hostvaluethatwillcausepublishtofail");
        service.publish("message");
    }

    /**
     * Use a valid host, but an invalid team domain
     */
    @Test
    public void invalidTeamDomainShouldFail() {
        StandardSlackService service = new StandardSlackService("my", "token", "#general");
        service.publish("message");
    }

    /**
     * Use a valid team domain, but a bad token
     */
    @Test
    public void invalidTokenShouldFail() {
        StandardSlackService service = new StandardSlackService("tinyspeck", "token", "#general");
        service.publish("message");
    }

    @Test
    public void publishToASingleRoomSendsASingleMessage() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "#room1");
        CloseableHttpClientStub closeableHttpClientStub = new CloseableHttpClientStub();
        service.setHttpClient(closeableHttpClientStub);
        service.publish("message");
        assertEquals(1, service.getHttpClient().getNumberOfCallsToExecuteMethod());
    }

    @Test
    public void publishToMultipleRoomsSendsAMessageToEveryRoom() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "#room1,#room2,#room3");
        CloseableHttpClientStub closeableHttpClientStub = new CloseableHttpClientStub();
        service.setHttpClient(closeableHttpClientStub);
        service.publish("message");
        assertEquals(3, service.getHttpClient().getNumberOfCallsToExecuteMethod());
    }

    @Test
    public void successfulPublishToASingleRoomReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "#room1");
        CloseableHttpClientStub closeableHttpClientStub = new CloseableHttpClientStub();
        closeableHttpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(closeableHttpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void successfulPublishToMultipleRoomsReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "#room1,#room2,#room3");
        CloseableHttpClientStub closeableHttpClientStub = new CloseableHttpClientStub();
        closeableHttpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(closeableHttpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void failedPublishToASingleRoomReturnsFalse() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "#room1");
        CloseableHttpClientStub closeableHttpClientStub = new CloseableHttpClientStub();
        closeableHttpClientStub.setHttpStatus(HttpStatus.SC_NOT_FOUND);
        service.setHttpClient(closeableHttpClientStub);
        assertFalse(service.publish("message"));
    }

    @Test
    public void singleFailedPublishToMultipleRoomsReturnsFalse() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "#room1,#room2,#room3");
        CloseableHttpClientStub closeableHttpClientStub = new CloseableHttpClientStub();
        closeableHttpClientStub.setFailAlternateResponses(true);
        closeableHttpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(closeableHttpClientStub);
        assertFalse(service.publish("message"));
    }

    @Test
    public void publishToEmptyRoomReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("domain", "token", "");
        CloseableHttpClientStub closeableHttpClientStub = new CloseableHttpClientStub();
        closeableHttpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(closeableHttpClientStub);
        assertTrue(service.publish("message"));
    }
}
