package jenkins.plugins.slack;

import jenkins.plugins.slack.StandardSlackService;
import org.junit.Before;
import org.junit.Test;

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
}
