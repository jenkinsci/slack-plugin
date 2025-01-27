package jenkins.plugins.slack;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.util.FormValidation;
import net.sf.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;

@WithJenkins
class SlackNotifierTest {

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private JenkinsRule rule;
    private SlackNotifierStub.DescriptorImplStub descriptor;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        this.rule = rule;
        descriptor = new SlackNotifierStub.DescriptorImplStub();
    }

    static Object[][] businessTypeKeys() {
        return new Object[][]{
                {new SlackServiceStub(), true, FormValidation.Kind.OK},
                {new SlackServiceStub(), false, FormValidation.Kind.ERROR},
                {null, false, FormValidation.Kind.ERROR}
        };
    }

    @ParameterizedTest
    @MethodSource("businessTypeKeys")
    void testDoTestConnection(SlackServiceStub slackServiceStub, boolean response, FormValidation.Kind expectedResult) {
        if (slackServiceStub != null) {
            slackServiceStub.setResponse(response);
        }
        descriptor.setSlackService(slackServiceStub);
        FormValidation result = descriptor
                .doTestConnection("baseUrl", "teamDomain", "authTokenCredentialId", false, "room", false, ":+1:", "slack", null);
        assertEquals(expectedResult, result.kind);
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

        @Override
        public boolean removeReaction(String channelId, String timestamp, String emojiName) {
            return response;
        }

        public void setResponse(boolean response) {
            this.response = response;
        }

        public String getResponseString() {
            return null;
        }

        @Override
        public boolean upload(FilePath workspace, String artifactIncludes, TaskListener log) {
            return this.response;
        }
    }
}
