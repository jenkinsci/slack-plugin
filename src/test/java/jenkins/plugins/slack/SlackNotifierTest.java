package jenkins.plugins.slack;

import hudson.model.*;
import hudson.util.FormValidation;
import junit.framework.TestCase;
import net.sf.json.JSONArray;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Arrays;
import java.util.Collection;

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
        FormValidation result = descriptor
                .doTestConnection("baseUrl", "teamDomain", "authToken", "authTokenCredentialId", false, "room");
        assertEquals(result.kind, expectedResult);
    }

    @Test
    public void testPerformConsoleLogEmptyGlobalConfig() {
        if (slackServiceStub != null) {
            slackServiceStub.setResponse(response);
        }
        descriptor.setSlackService(slackServiceStub);
        SlackNotifierStub Sn = new SlackNotifierStub("","","",false,"", "", "",
                false, false, false, false, false, false, false,
                false, false, false, false, null, false, "",
                "", "", "", "", "");
        Sn.setDescriptor(descriptor);
        java.io.ByteArrayOutputStream ba = new java.io.ByteArrayOutputStream(40);
        StreamBuildListener listener = new StreamBuildListener(ba);
        try {
            Boolean result = Sn.perform((AbstractBuild) null, null, listener);
            System.out.println(ba);
        } catch (Exception e)
        {
            System.out.println(e);
            assertEquals("[INFO] Slack notifications enabled in post-build but no Channel was found in the job or global config", ba.toString().trim());
        }

        //job room is set
        ba = new java.io.ByteArrayOutputStream(40);
        listener = new StreamBuildListener(ba);
        Sn.setRoom("foo");
        try {
            Boolean result = Sn.perform((AbstractBuild) null, null, listener);
            System.out.println(ba);
        } catch (Exception e)
        {
            System.out.println(e);
            assertEquals("[INFO] Slack notifications will be sent to the following channels: foo", ba.toString().trim());
        }

        //job room and subdomain set
        ba = new java.io.ByteArrayOutputStream(40);
        listener = new StreamBuildListener(ba);
        Sn.setRoom("bar");
        Sn.setTeamDomain("baz");
        try {
            Boolean result = Sn.perform((AbstractBuild) null, null, listener);
            System.out.println(ba);
        } catch (Exception e)
        {
            System.out.println(e);
            assertEquals("[INFO] Slack notifications will be sent to the following channels: bar on Team Subdomain: baz", ba.toString().trim());
        }
    }

    @Test
    public void testPerformConsoleLogWithGlobalConfig() {
        if (slackServiceStub != null) {
            slackServiceStub.setResponse(response);
        }
        descriptor.setSlackService(slackServiceStub);
        descriptor.setRoom("GlobalFoo, GlobalBar");
        descriptor.setTeamDomain("GlobalBaz");
        SlackNotifierStub Sn = new SlackNotifierStub("","","",false,"", "", "",
                false, false, false, false, false, false, false,
                false, false, false, false, null, false, "",
                "", "", "", "", "");
        Sn.setDescriptor(descriptor);
        java.io.ByteArrayOutputStream ba = new java.io.ByteArrayOutputStream(40);
        StreamBuildListener listener = new StreamBuildListener(ba);
        try {
            Boolean result = Sn.perform((AbstractBuild) null, null, listener);
            System.out.println(ba);
        } catch (Exception e)
        {
            System.out.println(e);
            assertEquals("[INFO] Slack notifications will be sent to the following channels: GlobalFoo, GlobalBar on Team Subdomain: GlobalBaz", ba.toString().trim());
        }

        //job room is set takes precedence
        ba = new java.io.ByteArrayOutputStream(40);
        listener = new StreamBuildListener(ba);
        Sn.setRoom("foo");
        try {
            Boolean result = Sn.perform((AbstractBuild) null, null, listener);
            System.out.println(ba);
        } catch (Exception e)
        {
            System.out.println(e);
            assertEquals("[INFO] Slack notifications will be sent to the following channels: foo on Team Subdomain: GlobalBaz", ba.toString().trim());
        }

        //job room and subdomain set
        ba = new java.io.ByteArrayOutputStream(40);
        listener = new StreamBuildListener(ba);
        Sn.setRoom("bar");
        Sn.setTeamDomain("baz");
        try {
            Boolean result = Sn.perform((AbstractBuild) null, null, listener);
            System.out.println(ba);
        } catch (Exception e)
        {
            System.out.println(e);
            assertEquals("[INFO] Slack notifications will be sent to the following channels: bar on Team Subdomain: baz", ba.toString().trim());
        }


    }

    public static class SlackServiceStub implements SlackService {

        private boolean response;

        public boolean publish(String message) {
            return response;
        }

        public boolean publish(String message, String color) {
            return response;
        }

        @Override
        public boolean publish(String message, JSONArray attachments, String color) {
            return response;
        }

        public void setResponse(boolean response) {
            this.response = response;
        }

        public String getResponseString() { return null; }
    }
}
