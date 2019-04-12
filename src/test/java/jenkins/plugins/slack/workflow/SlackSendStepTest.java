package jenkins.plugins.slack.workflow;

import hudson.model.Item;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.CredentialsObtainer;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.SlackService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Traditional Unit tests, allows testing null Jenkins.get()
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class, SlackSendStep.class, CredentialsObtainer.class})
public class SlackSendStepTest {

    @Mock
    TaskListener taskListenerMock;
    @Mock
    PrintStream printStreamMock;
    @Mock
    StepContext stepContextMock;
    @Mock
    Project project;
    @Mock
    Run run;
    @Mock
    SlackService slackServiceMock;
    @Mock
    Jenkins jenkins;
    @Mock
    SlackNotifier.DescriptorImpl slackDescMock;

    @Before
    public void setUp() throws IOException, InterruptedException {
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.mockStatic(CredentialsObtainer.class);
        when(jenkins.getDescriptorByType(SlackNotifier.DescriptorImpl.class)).thenReturn(slackDescMock);
        PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);
        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        when(stepContextMock.get(TaskListener.class)).thenReturn(taskListenerMock);
    }

    @Test
    public void testStepOverrides() throws Exception {
        final String token = "mytoken";
        SlackSendStep slackSendStep = new SlackSendStep();
        slackSendStep.setMessage("message");
        slackSendStep.setToken(token);
        slackSendStep.setTokenCredentialId("tokenCredentialId");
        slackSendStep.setBotUser(true);
        slackSendStep.setBaseUrl("baseUrl/");
        slackSendStep.setTeamDomain("teamDomain");
        slackSendStep.setChannel("channel");
        slackSendStep.setColor("good");
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(slackSendStep, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        PowerMockito.when(CredentialsObtainer.getTokenToUse(anyString(), any(Item.class), anyString())).thenReturn(token);

        when(stepContextMock.get(Project.class)).thenReturn(project);

        when(slackDescMock.isBotUser()).thenReturn(false);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);
        when(slackServiceMock.publish(anyString(), anyString())).thenReturn(true);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService("baseUrl/", "teamDomain", true, "channel", false, token);
        verify(slackServiceMock, times(1)).publish("message", "good");
    }

    @Test
    public void testStepWithAttachments() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        JSONArray attachments = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "Title of the message");
        jsonObject.put("author_name", "Name of the author");
        jsonObject.put("author_icon", "Avatar for author");
        attachments.add(jsonObject);
        step.setAttachments(attachments.toString());
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));
        ((JSONObject) attachments.get(0)).put("fallback", "message");

        when(Jenkins.get()).thenReturn(jenkins);

        PowerMockito.when(CredentialsObtainer.getTokenToUse(anyString(), any(Item.class), anyString())).thenReturn("token");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(slackServiceMock, times(0)).publish("message", "");
        verify(slackServiceMock, times(1)).publish("message", attachments, "");

    }

    @Test
    public void testStepWithAttachmentsAsListOfMap() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");

        Map<String, String> attachment1 = new HashMap<>();
        attachment1.put("title", "Title of the message");
        attachment1.put("author_name", "Name of the author");
        attachment1.put("author_icon", "Avatar for author");

        step.setAttachments(Arrays.asList(attachment1));
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        PowerMockito.when(CredentialsObtainer.getTokenToUse(anyString(), any(Item.class), anyString())).thenReturn("token");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(slackServiceMock, times(0)).publish("message", "");

        JSONArray expectedAttachments = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "Title of the message");
        jsonObject.put("author_name", "Name of the author");
        jsonObject.put("author_icon", "Avatar for author");
        jsonObject.put("fallback", "message");
        expectedAttachments.add(jsonObject);
        verify(slackServiceMock, times(1)).publish("message", expectedAttachments, "");
    }

    @Test
    public void testValuesForGlobalConfig() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        PowerMockito.when(CredentialsObtainer.getTokenToUse(eq("globalTokenCredentialId"), any(Item.class), anyString())).thenReturn("token2");

        when(stepContextMock.get(Project.class)).thenReturn(project);

        when(slackDescMock.getBaseUrl()).thenReturn("globalBaseUrl");
        when(slackDescMock.getTeamDomain()).thenReturn("globalTeamDomain");
        when(slackDescMock.getTokenCredentialId()).thenReturn("globalTokenCredentialId");
        when(slackDescMock.isBotUser()).thenReturn(false);
        when(slackDescMock.getRoom()).thenReturn("globalChannel");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService("globalBaseUrl", "globalTeamDomain", false, "globalChannel", false, "token2");
        verify(slackServiceMock, times(1)).publish("message", "");
    }


    @Test
    public void testCanGetItemFromRun() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));
        when(Jenkins.get()).thenReturn(jenkins);
        when(stepContextMock.get(Run.class)).thenReturn(run);
        when(run.getParent()).thenReturn(project);
        PowerMockito.when(CredentialsObtainer.getTokenToUse(anyString(), eq(project), anyString())).thenReturn("runcredentials");

        when(slackDescMock.getBaseUrl()).thenReturn("globalBaseUrl");
        when(slackDescMock.getTeamDomain()).thenReturn("globalTeamDomain");
        when(slackDescMock.getTokenCredentialId()).thenReturn("globalTokenCredentialId");
        when(slackDescMock.isBotUser()).thenReturn(false);
        when(slackDescMock.getRoom()).thenReturn("globalChannel");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        stepExecution.run();

        verify(stepExecution, times(1)).getSlackService("globalBaseUrl", "globalTeamDomain",
                false, "globalChannel", false, "runcredentials");
        verify(slackServiceMock, times(1)).publish("message", "");
    }

    @Test
    public void testReplyBroadcast() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setReplyBroadcast(true);

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        PowerMockito.when(CredentialsObtainer.getTokenToUse(eq("globalTokenCredentialId"), any(Item.class), anyString())).thenReturn("token");

        when(stepContextMock.get(Project.class)).thenReturn(project);

        when(slackDescMock.getBaseUrl()).thenReturn("globalBaseUrl");
        when(slackDescMock.getTeamDomain()).thenReturn("globalTeamDomain");
        when(slackDescMock.getTokenCredentialId()).thenReturn("globalTokenCredentialId");
        when(slackDescMock.isBotUser()).thenReturn(false);
        when(slackDescMock.getRoom()).thenReturn("globalChannel");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService("globalBaseUrl", "globalTeamDomain", false, "globalChannel", true, "token");
        verify(slackServiceMock, times(1)).publish("message", "");
    }

    @Test
    public void testNonNullEmptyColor() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setColor("");

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        PowerMockito.when(CredentialsObtainer.getTokenToUse(anyString(), any(Item.class), anyString())).thenReturn("token");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(slackServiceMock, times(1)).publish("message", "");
    }

    @Test
    public void testSlackResponseObject() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setToken("token");
        step.setTokenCredentialId("tokenCredentialId");
        step.setBotUser(true);
        step.setBaseUrl("baseUrl/");
        step.setTeamDomain("teamDomain");
        step.setChannel("channel");
        step.setColor("good");

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        PowerMockito.when(CredentialsObtainer.getTokenToUse(anyString(), any(Item.class), anyString())).thenReturn("token");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        String savedResponse = IOUtils.toString(
                this.getClass().getResourceAsStream("response.json")
        );
        when(slackServiceMock.getResponseString()).thenReturn(savedResponse);
        when(slackServiceMock.publish(anyString(), anyString())).thenReturn(true);

        SlackResponse response = stepExecution.run();
        String expectedId = "F4KE1DABC";
        String expectedTs = "1543931401.000500";
        String expectedThreadId = "F4KE1DABC:1543931401.000500";
        assertNotNull(response);
        assertEquals(expectedId, response.getChannelId());
        assertEquals(expectedTs, response.getTs());
        assertEquals(expectedThreadId, response.getThreadId());

    }

    @Test
    public void testSlackResponseObjectNullNonBotUser() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setToken("token");
        step.setTokenCredentialId("tokenCredentialId");
        step.setBotUser(false);
        step.setBaseUrl("baseUrl/");
        step.setTeamDomain("teamDomain");
        step.setChannel("channel");
        step.setColor("good");

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        PowerMockito.when(CredentialsObtainer.getTokenToUse(eq("tokenCredentialId"), any(Item.class), anyString())).thenReturn("token");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        when(slackServiceMock.getResponseString()).thenReturn(null);
        when(slackServiceMock.publish(anyString(), anyString())).thenReturn(true);

        SlackResponse response = stepExecution.run();
        assertNotNull(response);
        assertNull(response.getChannelId());
        assertNull(response.getTs());
        assertNull(response.getThreadId());
    }
}
