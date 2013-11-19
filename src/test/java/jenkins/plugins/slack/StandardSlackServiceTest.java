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
        StandardSlackService service = new StandardHipChatService("token", "room", "from");
        service.setHost("hostvaluethatwillcausepublishtofail");
        service.publish("message");
    }
}
