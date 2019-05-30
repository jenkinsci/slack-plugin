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
        StandardSlackService service = new StandardSlackService("", "foo", false, "#general", false, false, "token");
        service.setHost("hostvaluethatwillcausepublishtofail");
        service.publish("message");
    }

    /**
     * Use a valid host, but an invalid team domain
     */
    @Test
    public void invalidTeamDomainShouldFail() {
        StandardSlackService service = new StandardSlackService("", "my", false, "#general", false, false, "token");
        service.publish("message");
    }

    /**
     * Use a valid team domain, but a bad token
     */
    @Test
    public void invalidTokenShouldFail() {
        StandardSlackService service = new StandardSlackService("", "tinyspeck", false, "#general", false, false,"token");
        service.publish("message");
    }

    @Test
    public void publishToASingleRoomSendsASingleMessage() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", false, "#room1", "token");
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertEquals(1, service.getHttpClient().getNumberOfCallsToExecuteMethod());
    }

    @Test
    public void publishToMultipleRoomsSendsAMessageToEveryRoom() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", false, "#room1,#room2,#room3", "token");
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertEquals(3, service.getHttpClient().getNumberOfCallsToExecuteMethod());
    }

    @Test
    public void successfulPublishToASingleRoomReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", false, "#room1", "token");
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }


    @Test
    public void successfulPublishToSingleRoomWithProvidedTokenReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", false, "#room1", "providedtoken");
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void successfulPublishToMultipleRoomsReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", false, "#room1,#room2,#room3", "token");
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void failedPublishToASingleRoomReturnsFalse() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", false, "#room1", "token");
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_NOT_FOUND);
        service.setHttpClient(httpClientStub);
        assertFalse(service.publish("message"));
    }

    @Test
    public void singleFailedPublishToMultipleRoomsReturnsFalse() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain",false, "#room1,#room2,#room3", "token");
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setFailAlternateResponses(true);
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertFalse(service.publish("message"));
    }

    @Test
    public void publishToEmptyRoomReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", false, "", "token");
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }


    @Test
    public void sendAsBotUserReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", true, "#room1", "token");
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void sendAsBotUserInThreadReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", true, "#room1:1528317530", "token");
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void populatedTokenIsUsed() {
        final String populatedToken = "secret-text";
        final StandardSlackServiceStub service = new StandardSlackServiceStub("", "domain", true, "#room1:1528317530", populatedToken);
        final CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertTrue(httpClientStub.getLastRequest().getURI().toString().contains(populatedToken));
    }
}
