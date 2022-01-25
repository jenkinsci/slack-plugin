package jenkins.plugins.slack.workflow;

import hudson.model.Item;
import hudson.model.Project;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.CredentialsObtainer;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.SlackRequest;
import jenkins.plugins.slack.SlackService;
import jenkins.plugins.slack.user.NoSlackUserIdResolver;
import jenkins.plugins.slack.user.SlackUserIdResolver;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

/**
 * Traditional Unit tests, allows testing null Jenkins.get()
 */
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

    @Mock
    Item item;

    private AutoCloseable mocks;
    private MockedStatic<Jenkins> mockedJenkins;
    private MockedStatic<CredentialsObtainer> credentialsObtainerMockedStatic;

    @Before
    public void setUp() throws IOException, InterruptedException {
        mocks = openMocks(this);

        mock(CredentialsObtainer.class);
        when(jenkins.getDescriptorByType(SlackNotifier.DescriptorImpl.class)).thenReturn(slackDescMock);
        credentialsObtainerMockedStatic = mockStatic(CredentialsObtainer.class);
        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getItemForCredentials(any())).thenReturn(item);
        mockedJenkins = mockStatic(Jenkins.class);
        Jenkins jenkins = mock(Jenkins.class);
        mockedJenkins.when(Jenkins::get).thenReturn(jenkins);
        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        when(stepContextMock.get(Run.class)).thenReturn(run);
        when(stepContextMock.get(TaskListener.class)).thenReturn(taskListenerMock);
    }

    @After
    public void fin() throws Exception {
        mocks.close();
        mockedJenkins.close();
        credentialsObtainerMockedStatic.close();
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
        slackSendStep.setIconEmoji(":+1:");
        slackSendStep.setUsername("slack");
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(slackSendStep, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(anyString(), any(Item.class), any())).thenReturn(token);

        when(stepContextMock.get(Project.class)).thenReturn(project);

        when(slackDescMock.isBotUser()).thenReturn(false);
        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(eq(run), anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), anyString(), anyString(), anyString(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);
        when(slackServiceMock.publish(anyString(), anyString())).thenReturn(true);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService(run, "baseUrl/", "teamDomain",
                true, "channel", false, false, ":+1:", "slack", token, false, noSlackUserIdResolver);
        verify(slackServiceMock, times(1)).publish("message", "good");
    }

    @Test
    public void testStepWithAttachments() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setTokenCredentialId("tokenCredentialId");
        step.setBotUser(true);
        step.setTeamDomain("teamDomain");
        step.setChannel("channel");

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

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(anyString(), any(Item.class), any())).thenReturn("token");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(eq(run), any(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(slackServiceMock, times(0)).publish("message", "");

        SlackRequest expectedSlackRequest = SlackRequest.builder()
                .withAttachments(attachments)
                .withMessage("message")
                .withColor("").build();
        verify(slackServiceMock, times(1)).publish(expectedSlackRequest);

    }

    @Test
    public void testStepWithBlocks() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setTokenCredentialId("tokenCredentialId");
        step.setBotUser(true);
        step.setTeamDomain("teamDomain");
        step.setChannel("channel");

        Map<String, String> blocks = new HashMap<>();
        blocks.put("title", "Title of the message");
        blocks.put("author_name", "Name of the author");
        blocks.put("author_icon", "Avatar for author");

        step.setBlocks(Collections.singletonList(blocks));

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(anyString(), any(Item.class), any())).thenReturn("token");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(eq(run), any(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(slackServiceMock, times(0)).publish("message", "");


        JSONArray expectedBlocks = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "Title of the message");
        jsonObject.put("author_name", "Name of the author");
        jsonObject.put("author_icon", "Avatar for author");
        expectedBlocks.add(jsonObject);

        SlackRequest slackRequest = SlackRequest.builder()
                .withMessage("message")
                .withBlocks(expectedBlocks)
                .build();
        verify(slackServiceMock, times(1)).publish(slackRequest);
    }

    @Test
    public void testStepWithAttachmentsAsListOfMap() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setTokenCredentialId("tokenCredentialId");
        step.setBotUser(true);
        step.setTeamDomain("teamDomain");
        step.setChannel("channel");

        Map<String, String> attachment1 = new HashMap<>();
        attachment1.put("title", "Title of the message");
        attachment1.put("author_name", "Name of the author");
        attachment1.put("author_icon", "Avatar for author");

        step.setAttachments(Collections.singletonList(attachment1));
        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(anyString(), any(Item.class), any())).thenReturn("token");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        when(stepExecution.getSlackService(eq(run), any(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(slackServiceMock, times(0)).publish("message", "");

        JSONArray expectedAttachments = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", "Title of the message");
        jsonObject.put("author_name", "Name of the author");
        jsonObject.put("author_icon", "Avatar for author");
        jsonObject.put("fallback", "message");
        expectedAttachments.add(jsonObject);

        SlackRequest expectedSlackRequest = SlackRequest.builder()
                .withAttachments(expectedAttachments)
                .withMessage("message")
                .withColor("").build();
        verify(slackServiceMock, times(1)).publish(expectedSlackRequest);
    }

    @Test
    public void testValuesForGlobalConfig() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(eq("globalTokenCredentialId"), any(Item.class), any())).thenReturn("token2");

        when(stepContextMock.get(Project.class)).thenReturn(project);

        when(slackDescMock.getBaseUrl()).thenReturn("globalBaseUrl");
        when(slackDescMock.getTeamDomain()).thenReturn("globalTeamDomain");
        when(slackDescMock.getTokenCredentialId()).thenReturn("globalTokenCredentialId");
        when(slackDescMock.isBotUser()).thenReturn(false);
        when(slackDescMock.getRoom()).thenReturn("globalChannel");
        when(slackDescMock.getIconEmoji()).thenReturn(":+1:");
        when(slackDescMock.getUsername()).thenReturn("slack");

        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(eq(run), anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService(run, "globalBaseUrl", "globalTeamDomain",
                false, "globalChannel", false, false, ":+1:", "slack", "token2", false, noSlackUserIdResolver);
        verify(slackServiceMock, times(1)).publish("message", "");
    }


    @Test
    public void testCanGetItemFromRun() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));
        when(Jenkins.get()).thenReturn(jenkins);
        when(run.getParent()).thenReturn(project);

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(any(), any(), any())).thenReturn("runcredentials");

        when(slackDescMock.getBaseUrl()).thenReturn("globalBaseUrl");
        when(slackDescMock.getTeamDomain()).thenReturn("globalTeamDomain");
        when(slackDescMock.getTokenCredentialId()).thenReturn("globalTokenCredentialId");
        when(slackDescMock.isBotUser()).thenReturn(false);
        when(slackDescMock.getRoom()).thenReturn("globalChannel");
        when(slackDescMock.getIconEmoji()).thenReturn(":+1:");
        when(slackDescMock.getUsername()).thenReturn("slack");

        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(eq(run), anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

        stepExecution.run();

        verify(stepExecution, times(1)).getSlackService(run, "globalBaseUrl", "globalTeamDomain",
                false, "globalChannel", false, false, ":+1:", "slack", "runcredentials", false, noSlackUserIdResolver);
        verify(slackServiceMock, times(1)).publish("message", "");
    }

    @Test
    public void testReplyBroadcast() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setReplyBroadcast(true);

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(eq("globalTokenCredentialId"), any(), any())).thenReturn("token");

        when(stepContextMock.get(Project.class)).thenReturn(project);

        when(slackDescMock.getBaseUrl()).thenReturn("globalBaseUrl");
        when(slackDescMock.getTeamDomain()).thenReturn("globalTeamDomain");
        when(slackDescMock.getTokenCredentialId()).thenReturn("globalTokenCredentialId");
        when(slackDescMock.isBotUser()).thenReturn(false);
        when(slackDescMock.getRoom()).thenReturn("globalChannel");
        when(slackDescMock.getIconEmoji()).thenReturn(":+1:");
        when(slackDescMock.getUsername()).thenReturn("slack");

        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(eq(run), anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService(run, "globalBaseUrl", "globalTeamDomain",
                false, "globalChannel", true, false, ":+1:", "slack", "token", false, noSlackUserIdResolver);
        verify(slackServiceMock, times(1)).publish("message", "");
    }

    @Test
    public void testSendAsText() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setSendAsText(true);

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(eq("globalTokenCredentialId"), any(), any())).thenReturn("token");

        when(stepContextMock.get(Project.class)).thenReturn(project);

        when(slackDescMock.getBaseUrl()).thenReturn("globalBaseUrl");
        when(slackDescMock.getTeamDomain()).thenReturn("globalTeamDomain");
        when(slackDescMock.getTokenCredentialId()).thenReturn("globalTokenCredentialId");
        when(slackDescMock.isBotUser()).thenReturn(false);
        when(slackDescMock.getRoom()).thenReturn("globalChannel");
        when(slackDescMock.getIconEmoji()).thenReturn(":+1:");
        when(slackDescMock.getUsername()).thenReturn("slack");

        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(eq(run), anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService(run, "globalBaseUrl", "globalTeamDomain", false, "globalChannel",
                false, true, ":+1:", "slack", "token", false, noSlackUserIdResolver);
        verify(slackServiceMock, times(1)).publish("message", new JSONArray(), "");
    }

    @Test
    public void testIconEmoji() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setIconEmoji(":+1:");

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(eq("globalTokenCredentialId"), any(), any())).thenReturn("token");

        when(stepContextMock.get(Project.class)).thenReturn(project);

        when(slackDescMock.getBaseUrl()).thenReturn("globalBaseUrl");
        when(slackDescMock.getTeamDomain()).thenReturn("globalTeamDomain");
        when(slackDescMock.getTokenCredentialId()).thenReturn("globalTokenCredentialId");
        when(slackDescMock.isBotUser()).thenReturn(false);
        when(slackDescMock.getRoom()).thenReturn("globalChannel");
        when(slackDescMock.getUsername()).thenReturn("slack");

        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(eq(run), anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService(run, "globalBaseUrl", "globalTeamDomain",
                false, "globalChannel", false, false, ":+1:", "slack","token", false, noSlackUserIdResolver);
        verify(slackServiceMock, times(1)).publish("message", "");
    }

    @Test
    public void testUsername() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setUsername("username");

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(eq("globalTokenCredentialId"), any(), any())).thenReturn("token");

        when(stepContextMock.get(Project.class)).thenReturn(project);

        when(slackDescMock.getBaseUrl()).thenReturn("globalBaseUrl");
        when(slackDescMock.getTeamDomain()).thenReturn("globalTeamDomain");
        when(slackDescMock.getTokenCredentialId()).thenReturn("globalTokenCredentialId");
        when(slackDescMock.isBotUser()).thenReturn(false);
        when(slackDescMock.getRoom()).thenReturn("globalChannel");
        when(slackDescMock.getIconEmoji()).thenReturn(":+1:");

        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(eq(run), anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService(run, "globalBaseUrl", "globalTeamDomain",
                false, "globalChannel", false, false, ":+1:", "username","token", false, noSlackUserIdResolver);
        verify(slackServiceMock, times(1)).publish("message", "");
    }

    @Test
    public void testTimestamp() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setUsername("username");
        step.setTimestamp("1241242.124124");

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(eq("globalTokenCredentialId"), any(), any())).thenReturn("token");

        when(stepContextMock.get(Project.class)).thenReturn(project);

        when(slackDescMock.getBaseUrl()).thenReturn("globalBaseUrl");
        when(slackDescMock.getTeamDomain()).thenReturn("globalTeamDomain");
        when(slackDescMock.getTokenCredentialId()).thenReturn("globalTokenCredentialId");
        when(slackDescMock.isBotUser()).thenReturn(false);
        when(slackDescMock.getRoom()).thenReturn("globalChannel");
        when(slackDescMock.getIconEmoji()).thenReturn(":+1:");

        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(eq(run), anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

        stepExecution.run();
        verify(stepExecution, times(1)).getSlackService(run, "globalBaseUrl", "globalTeamDomain",
                false, "globalChannel", false, false, ":+1:", "username","token", false, noSlackUserIdResolver);
        verify(slackServiceMock, times(1)).publish("message", "", "1241242.124124");
    }

    @Test
    public void testNonNullEmptyColor() throws Exception {
        SlackSendStep step = new SlackSendStep();
        step.setMessage("message");
        step.setColor("");
        step.setTokenCredentialId("tokenCredentialId");
        step.setBotUser(true);
        step.setTeamDomain("teamDomain");
        step.setChannel("channel");

        SlackSendStep.SlackSendStepExecution stepExecution = spy(new SlackSendStep.SlackSendStepExecution(step, stepContextMock));

        when(Jenkins.get()).thenReturn(jenkins);

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(anyString(), any(Item.class), any())).thenReturn("token");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        when(stepExecution.getSlackService(eq(run), any(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

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

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(anyString(), any(Item.class), any())).thenReturn("token");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        when(stepExecution.getSlackService(eq(run), anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

        String savedResponse = IOUtils.toString(
                this.getClass().getResourceAsStream("response.json"),
                StandardCharsets.UTF_8
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

        NoSlackUserIdResolver noSlackUserIdResolver = new NoSlackUserIdResolver();
        when(slackDescMock.getSlackUserIdResolver()).thenReturn(noSlackUserIdResolver);

        credentialsObtainerMockedStatic.when(() -> CredentialsObtainer.getTokenToUse(eq("tokenCredentialId"), any(Item.class), any())).thenReturn("token");

        when(taskListenerMock.getLogger()).thenReturn(printStreamMock);
        doNothing().when(printStreamMock).println();

        when(stepExecution.getSlackService(eq(run), anyString(), anyString(), anyBoolean(), anyString(), anyBoolean(), anyBoolean(), any(), any(), any(), anyBoolean(), any(SlackUserIdResolver.class))).thenReturn(slackServiceMock);

        when(slackServiceMock.getResponseString()).thenReturn(null);
        when(slackServiceMock.publish(anyString(), anyString())).thenReturn(true);

        SlackResponse response = stepExecution.run();
        assertNotNull(response);
        assertNull(response.getChannelId());
        assertNull(response.getTs());
        assertNull(response.getThreadId());
    }
}
