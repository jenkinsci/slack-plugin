package jenkins.plugins.slack;

import hudson.FilePath;
import hudson.util.FormValidation;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import junit.framework.TestCase;
import net.sf.json.JSONArray;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

@RunWith(Parameterized.class)
public class SlackNotifierTest extends TestCase {

    private SlackNotifierStub.DescriptorImplStub descriptor;
    private SlackServiceStub slackServiceStub;
    private boolean response;
    private FormValidation.Kind expectedResult;

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
    }

    @Parameterized.Parameters
    public static Collection<Object[]> businessTypeKeys() {
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
        FormValidation result = descriptor
                .doTestConnection("baseUrl", "teamDomain", "authTokenCredentialId", false, "room", false, ":+1:", "slack", null);
        assertEquals(result.kind, expectedResult);
    }

    public static class SlackServiceStub implements SlackService {

        private boolean response;

        public boolean publish(String message) {
            return response;
        }

        @Override
        public boolean publish(SlackRequest slackRequest) {
            return response;
        }

        public boolean publish(String message, String color) {
            return response;
        }

        @Override
        public boolean publish(String message, String color, String timestamp) {
            return response;
        }

        @Override
        public boolean publish(String message, JSONArray attachments, String color) {
            return response;
        }

        @Override
        public boolean publish(String message, JSONArray attachments, String color, String timestamp) {
            return response;
        }

        @Override
        public boolean addReaction(String channelId, String timestamp, String emojiName) {
            return response;
        }

        public void setResponse(boolean response) {
            this.response = response;
        }

        public String getResponseString() {
            return null;
        }

        @Override
        public boolean upload(FilePath workspace, String artifactIncludes, PrintStream log) {
            return this.response;
        }
    }
}
