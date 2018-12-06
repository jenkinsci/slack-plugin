package jenkins.plugins.slack.workflow;

import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.Messages;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.SlackService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.PrintStream;
import java.io.PrintWriter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Traditional Unit tests, allows testing null Jenkins.getActiveInstance()
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Jenkins.class,SlackSendStep.class})
public class SlackSendStepTest {

    @Mock
    TaskListener taskListenerMock;
    @Mock
    PrintStream printStreamMock;
    @Mock
    PrintWriter printWriterMock;
    @Mock
    StepContext stepContextMock;
    @Mock
    SlackService slackServiceMock;
    @Mock
    Jenkins jenkins;
    @Mock
    SlackNotifier.DescriptorImpl slackDescMock;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Jenkins.class);
        when(jenkins.getDescriptorByType(SlackNotifier.DescriptorImpl.class)).thenReturn(slackDescMock);
    }

    @Test
    public void testStepOverrides() throws Exception {
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution());
        SlackSendStep slackSendStep = new SlackSendStep();
        slackSendStep.setMessage("message");
        slackSendStep.setToken("token");
        slackSendStep.setTokenCredentialId("tokenCredentialId");
        slackSendStep.setBotUser(false);
        slackSendStep.setBaseUrl("baseUrl/");
        slackSendStep.setTeamDomain("teamDomain");
        slackSendStep.setChannel("channel");
        slackSendStep.setColor("good");
        stepExecution.step = slackSendStep;

        when(Jenkins.getActiveInstance()).thenReturn(jenkins);

        stepExecution.listener = taskListenerMock;

        when(slackDescMock.getToken()).thenReturn("differentToken");
        when(slackDescMock.isBotUser()).thenReturn(true);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);
        when(slackServiceMock.publish(anyString(), anyString())).thenReturn(true);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService("baseUrl/", "teamDomain", "token", "tokenCredentialId", false, "channel");
        verify(slackServiceMock, times(1)).publish("message", "good");
        assertFalse(stepExecution.step.isFailOnError());
    }

    @Test
    public void testStepOverrides2() throws Exception {
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution());
        SlackSendStep slackSendStep = new SlackSendStep();
        slackSendStep.setMessage("message");
        slackSendStep.setToken("token");
        slackSendStep.setTokenCredentialId("tokenCredentialId");
        slackSendStep.setBotUser(false);
        slackSendStep.setBaseUrl("baseUrl");
        slackSendStep.setTeamDomain("teamDomain");
        slackSendStep.setChannel("channel");
        slackSendStep.setColor("good");
        stepExecution.step = slackSendStep;

        when(Jenkins.getActiveInstance()).thenReturn(jenkins);

        stepExecution.listener = taskListenerMock;

        when(slackDescMock.getToken()).thenReturn("differentToken");
        when(slackDescMock.isBotUser()).thenReturn(true);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);
        when(slackServiceMock.publish(anyString(), anyString())).thenReturn(true);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService("baseUrl/", "teamDomain", "token", "tokenCredentialId", false, "channel");
        verify(slackServiceMock, times(1)).publish("message", "good");
        assertFalse(stepExecution.step.isFailOnError());
    }

    @Test
    public void testStepWithAttachments() throws Exception {
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution());
        stepExecution.step = new SlackSendStep();
        stepExecution.step.setMessage("message");
        JSONArray attachments = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title","Title of the message");
        jsonObject.put("author_name","Name of the author");
        jsonObject.put("author_icon","Avatar for author");
        attachments.add(jsonObject);
        stepExecution.step.setAttachments(attachments.toString());
        ((JSONObject) attachments.get(0)).put("fallback", "message");

        when(Jenkins.getActiveInstance()).thenReturn(jenkins);

        stepExecution.listener = taskListenerMock;


        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(slackServiceMock, times(0)).publish("message", "");
        verify(slackServiceMock, times(1)).publish(attachments, "");

    }

    @Test
    public void testValuesForGlobalConfig() throws Exception {

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution());
        stepExecution.step = new SlackSendStep();
        stepExecution.step.setMessage("message");

        when(Jenkins.getActiveInstance()).thenReturn(jenkins);

        stepExecution.listener = taskListenerMock;

        when(slackDescMock.getBaseUrl()).thenReturn("globalBaseUrl");
        when(slackDescMock.getTeamDomain()).thenReturn("globalTeamDomain");
        when(slackDescMock.getToken()).thenReturn("globalToken");
        when(slackDescMock.getTokenCredentialId()).thenReturn("globalTokenCredentialId");
        when(slackDescMock.isBotUser()).thenReturn(false);
        when(slackDescMock.getRoom()).thenReturn("globalChannel");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService("globalBaseUrl", "globalTeamDomain", "globalToken", "globalTokenCredentialId", false, "globalChannel");
        verify(slackServiceMock, times(1)).publish("message", "");
        assertNull(stepExecution.step.getBaseUrl());
        assertNull(stepExecution.step.getTeamDomain());
        assertNull(stepExecution.step.getToken());
        assertNull(stepExecution.step.getTokenCredentialId());
        assertNull(stepExecution.step.getChannel());
        assertNull(stepExecution.step.getColor());
    }

    @Test
    public void testNonNullEmptyColor() throws Exception {

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution());
        SlackSendStep slackSendStep = new SlackSendStep();
        slackSendStep.setMessage("message");
        slackSendStep.setColor("");
        stepExecution.step = slackSendStep;

        when(Jenkins.getActiveInstance()).thenReturn(jenkins);

        stepExecution.listener = taskListenerMock;

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(anyString(), anyString(), anyString(), anyString(), anyBoolean(), anyString())).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(slackServiceMock, times(1)).publish("message", "");
        assertNull(stepExecution.step.getColor());
    }
}
