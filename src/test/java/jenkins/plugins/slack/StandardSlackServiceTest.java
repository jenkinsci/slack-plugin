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
        StandardSlackService service = new StandardSlackService(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("foo")
                        .withBotUser(false)
                        .withRoomId("#general")
                        .withReplyBroadcast(false)
                        .withPopulatedToken("token"));
        service.setHost("hostvaluethatwillcausepublishtofail");
        service.publish("message");
    }

    /**
     * Use a valid host, but an invalid team domain
     */
    @Test
    public void invalidTeamDomainShouldFail() {
        StandardSlackService service = new StandardSlackService(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("my")
                        .withBotUser(false)
                        .withRoomId("#general")
                        .withReplyBroadcast(false)
                        .withPopulatedToken("token"));
        service.publish("message");
    }

    /**
     * Use a valid team domain, but a bad token
     */
    @Test
    public void invalidTokenShouldFail() {
        StandardSlackService service = new StandardSlackService(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("tinyspeck")
                        .withBotUser(false)
                        .withRoomId("#general")
                        .withReplyBroadcast(false)
                        .withPopulatedToken("token"));
        service.publish("message");
    }

    @Test
    public void publishToASingleRoomSendsASingleMessage() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(false)
                        .withRoomId("#room1")
                        .withPopulatedToken("token"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertEquals(1, service.getHttpClient().getNumberOfCallsToExecuteMethod());
    }

    @Test
    public void publishToMultipleRoomsSendsAMessageToEveryRoom() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(false)
                        .withRoomId("#room1,#room2,#room3")
                        .withPopulatedToken("token"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertEquals(3, service.getHttpClient().getNumberOfCallsToExecuteMethod());
    }

    @Test
    public void successfulPublishToASingleRoomReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(false)
                        .withRoomId("#room1")
                        .withPopulatedToken("token"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }


    @Test
    public void successfulPublishToSingleRoomWithProvidedTokenReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(false)
                        .withRoomId("#room1")
                        .withPopulatedToken("providedtoken"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void successfulPublishToMultipleRoomsReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(false)
                        .withRoomId("#room1,#room2,#room3")
                        .withPopulatedToken("token"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void failedPublishToASingleRoomReturnsFalse() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(false)
                        .withRoomId("#room1")
                        .withPopulatedToken("token"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_NOT_FOUND);
        service.setHttpClient(httpClientStub);
        assertFalse(service.publish("message"));
    }

    @Test
    public void singleFailedPublishToMultipleRoomsReturnsFalse() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(false)
                        .withRoomId("#room1,#room2,#room3")
                        .withPopulatedToken("token"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setFailAlternateResponses(true);
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertFalse(service.publish("message"));
    }

    @Test
    public void publishToEmptyRoomReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(false)
                        .withRoomId("")
                        .withPopulatedToken("token"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }


    @Test
    public void sendAsBotUserReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(true)
                        .withRoomId("#room1")
                        .withPopulatedToken("token"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void sendAsBotUserInThreadReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(true)
                        .withRoomId("#room1:1528317530")
                        .withPopulatedToken("token"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void populatedTokenIsUsed() {
        final String populatedToken = "secret-text";
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(true)
                        .withRoomId("#room1:1528317530")
                        .withPopulatedToken(populatedToken));
        final CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        service.publish("message");
        assertTrue(httpClientStub.getLastRequest().getHeaders("Authorization")[0].getValue().contains(populatedToken));
    }

    @Test
    public void iconEmojiAndBotUserReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(true)
                        .withRoomId("#room1")
                        .withPopulatedToken("token")
                        .withIconEmoji(":+1:"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void usernameAndBotUserReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(true)
                        .withRoomId("#room1")
                        .withPopulatedToken("token")
                        .withUsername("username"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void iconEmojiAndNotBotUserReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(false)
                        .withRoomId("#room1")
                        .withPopulatedToken("token")
                        .withIconEmoji(":+1:"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }

    @Test
    public void usernameAndNotBotUserReturnsTrue() {
        StandardSlackServiceStub service = new StandardSlackServiceStub(
                StandardSlackService.builder()
                        .withBaseUrl("")
                        .withTeamDomain("domain")
                        .withBotUser(false)
                        .withRoomId("#room1")
                        .withPopulatedToken("token")
                        .withUsername("username"));
        CloseableHttpClientStub httpClientStub = new CloseableHttpClientStub();
        httpClientStub.setHttpStatus(HttpStatus.SC_OK);
        service.setHttpClient(httpClientStub);
        assertTrue(service.publish("message"));
    }

}
