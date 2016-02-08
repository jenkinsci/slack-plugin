package jenkins.plugins.slack;

import hudson.model.Descriptor;
import hudson.util.FormValidation;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class SlackNotifierTest extends TestCase {

    private SlackNotifierStub.DescriptorImplStub descriptor;
    private SlackServiceStub slackServiceStub;
    private boolean response;
    private FormValidation.Kind expectedResult;

    @Before
    @Override
    public void setUp() {
        descriptor = new SlackNotifierStub.DescriptorImplStub();
    }

    public SlackNotifierTest(SlackServiceStub slackServiceStub, boolean response, FormValidation.Kind expectedResult) {
        this.slackServiceStub = slackServiceStub;
        this.response = response;
        this.expectedResult = expectedResult;
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
            FormValidation result = descriptor.doTestConnection("teamDomain", "authToken", "room", "apiToken", "buildServerUrl");
            assertEquals(result.kind, expectedResult);
        } catch (Descriptor.FormException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public static class SlackServiceStub implements SlackService {

        private boolean response;
        private String username = "";

        public boolean publish(String message) {
            return response;
        }

        public boolean publish(String message, String color) {
            return response;
        }

        public String getUserId(String email) {
            return null;
        }

        public void setResponse(boolean response) {
            this.response = response;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
