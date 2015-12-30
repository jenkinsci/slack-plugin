package jenkins.plugins.slack;

import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.util.FormValidation;
import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

@RunWith(Parameterized.class)
public class SlackNotifierTest extends TestCase {

    private SlackNotifierStub.DescriptorImplStub descriptor;
    private SlackServiceStub slackServiceStub;
    private boolean response;
    private FormValidation.Kind expectedResult;
    private SlackNotifierStub slackNotifier;

    @Rule
    public final JenkinsRule rule = new JenkinsRule();

    @Before
    @Override
    public void setUp() {
        descriptor = new SlackNotifierStub.DescriptorImplStub();
    }

    public SlackNotifierTest(SlackServiceStub slackServiceStub, boolean response, FormValidation.Kind expectedResult) {
        this.slackServiceStub = slackServiceStub;
        this.response = response;
        this.expectedResult = expectedResult;

        HashMap<String, String> customMessageMap = new HashMap<String, String>();
        customMessageMap.put("SUCCESS", "This is the Success Result Message");
        customMessageMap.put("UNSTABLE", "This is the Unstable Result Message");
        customMessageMap.put("NOT_BUILT", "This is the Not Built Result Message");
        customMessageMap.put("ABORTED", "This is the Aborted Result Message");
        customMessageMap.put("FAILURE", "This is the Failure Result Message");

        this.slackNotifier = new SlackNotifierStub( "teamDomain", "authToken", true, "room", "buildServerUrl", "sendAs", true, true, true, true, true, true, true, true, false, false, CommitInfoChoice.NONE, true, "Custom Message String", customMessageMap);
    }

    @Parameterized.Parameters
    public static Collection businessTypeKeys() {
        return Arrays.asList(new Object[][]{
                {new SlackServiceStub(), true, FormValidation.Kind.OK},
                {new SlackServiceStub(), false, FormValidation.Kind.ERROR},
                {null, false, FormValidation.Kind.ERROR}
        });
    }

    @Test
    public void testDoTestConnection() {
        if (slackServiceStub != null) {
            slackServiceStub.setResponse(response);
        }
        descriptor.setSlackService(slackServiceStub);
        try {
            FormValidation result = descriptor.doTestConnection("teamDomain", "authToken", "authTokenCredentialId", false,"room");
            assertEquals(result.kind, expectedResult);
        } catch (Descriptor.FormException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    /**
     * Test's the default custom message functionality
     */
    @Test
    public void testSlackNotifierCustomMessage() {
        String message = slackNotifier.getCustomMessage();
        assertEquals("Custom Message String", message);
    }

    /**
     * Test's enhanced Custom Message functionality
     */
    @Test
    public void testSlackNotifierAdvancedCustomMessageResults() {
        String message = slackNotifier.getCustomMessage( Result.SUCCESS );
        assertEquals("This is the Success Result Message", message);

        message = slackNotifier.getCustomMessage( Result.ABORTED );
        assertEquals("This is the Aborted Result Message", message);

        message = slackNotifier.getCustomMessage( Result.FAILURE );
        assertEquals("This is the Failure Result Message", message);

        message = slackNotifier.getCustomMessage( Result.NOT_BUILT );
        assertEquals("This is the Not Built Result Message", message);

        message = slackNotifier.getCustomMessage( Result.UNSTABLE );
        assertEquals("This is the Unstable Result Message", message);
    }

    /**
     * Test's enhanced Custom Message functionality
     */
    @Test
    public void testSlackNotifierAdvancedCustomMessageString() {
        String message = slackNotifier.getCustomMessage( "SUCCESS" );
        assertEquals("This is the Success Result Message", message);

        message = slackNotifier.getCustomMessage( "ABORTED" );
        assertEquals("This is the Aborted Result Message", message);

        message = slackNotifier.getCustomMessage( "FAILURE" );
        assertEquals("This is the Failure Result Message", message);

        message = slackNotifier.getCustomMessage( "NOT_BUILT" );
        assertEquals("This is the Not Built Result Message", message);

        message = slackNotifier.getCustomMessage( "UNSTABLE" );
        assertEquals("This is the Unstable Result Message", message);
    }

    public static class SlackServiceStub implements SlackService {

        private boolean response;

        public boolean publish(String message) {
            return response;
        }

        public boolean publish(String message, String color) {
            return response;
        }

        public void setResponse(boolean response) {
            this.response = response;
        }
    }
}
