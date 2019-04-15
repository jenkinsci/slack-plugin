package jenkins.plugins.slack.webhook;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import hudson.model.FreeStyleProject;
import jenkins.model.GlobalConfiguration;
import jenkins.plugins.slack.webhook.model.JsonResponse;
import jenkins.plugins.slack.webhook.model.SlackPostData;
import jenkins.plugins.slack.webhook.model.SlackTextMessage;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.kohsuke.stapler.RequestImpl;
import org.mockito.Mockito;

import static com.gargoylesoftware.htmlunit.HttpMethod.POST;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebhookEndpointTest {

    private RequestImpl req;

    private static final String URL = "webook";
    private static final String ENDPOINT = URL + "/";
    private static final String LONG_PROJECT_NAME = "slack_plugin";

    private WebhookEndpoint endpoint;
    private SlackPostData data;

    @Rule
    public final JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setUp() {
        endpoint = new WebhookEndpoint();
        req = mock(RequestImpl.class);
        data = new SlackPostData();
        data.setToken("GOOD_TOKEN");
        data.setTrigger_word("jenkins");
        when(req.bindJSON(Mockito.eq(SlackPostData.class), Mockito.any(JSONObject.class))).thenReturn(data);
    }

    @Test
    public void testNotNullOutgoingWebhookUrl() {
        WebhookEndpoint endpoint = new WebhookEndpoint();
        String url = endpoint.getUrlName();
        assertThat(url, is(not(nullValue())));
        assertThat(url.isEmpty(), is(false));
    }

    @Test
    public void testUnconfiguredSlackURL() throws Exception {
        WebClient client = jenkinsRule.createWebClient();
        WebRequest request =
            new WebRequest(client.createCrumbedUrl(ENDPOINT), POST);
        WebResponse response = client.loadWebResponse(request);
        assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));
    }

    @Test
    public void testUnconfiguredSlackToken() throws Exception {
        JsonResponse response = (JsonResponse) endpoint.doIndex(req);
        assertThat(response.getStatus(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("Slack token not set"));
    }

    @Test
    public void testNoTextPostData() throws Exception {
        setConfigSettings();
        JsonResponse response = (JsonResponse) endpoint.doIndex(req);
        assertThat(response.getStatus(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("Invalid command, text field required"));
    }

    @Test
    public void testNoTriggerWordPostData() throws Exception {
        // No trigger word is present, which is the case when Slack "slash commands" are used
        setConfigSettings();
        data.setTrigger_word(null);
        data.setText("list projects");
        JsonResponse response = (JsonResponse) endpoint.doIndex(req);
        assertThat(response.getStatus(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("*Projects:*\n>_No projects found_"));
    }

    @Test
    public void testInvalidConfiguredSlackToken() throws Exception {
        setConfigSettings();
        data.setToken("BAD_TOKEN");
        JsonResponse response = (JsonResponse) endpoint.doIndex(req);
        assertThat(response.getStatus(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("Invalid Slack token"));
    }

    @Test
    public void testInvalidTriggerWord() throws Exception {
        setConfigSettings();
        data.setTrigger_word("notJenkins");
        data.setText("jenkins list projects");
        JsonResponse response = (JsonResponse) endpoint.doIndex(req);
        assertThat(response.getStatus(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("Invalid command, invalid trigger_word"));
    }

    @Test
    public void testListProjects() throws Exception {
        setConfigSettings();
        data.setText("jenkins list projects");
        JsonResponse response = (JsonResponse) endpoint.doIndex(req);
        assertThat(response.getStatus(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("*Projects:*\n>_No projects found_"));

        FreeStyleProject project = jenkinsRule.createFreeStyleProject(LONG_PROJECT_NAME);
        response = (JsonResponse) endpoint.doIndex(req);
        assertThat(response.getStatus(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("*Projects:*\n>*" + LONG_PROJECT_NAME + "*\n>*Last Build:* #TBD\n>*Status:* TBD\n\n\n"));

        project.scheduleBuild2(0).get();
        response = (JsonResponse) endpoint.doIndex(req);
        assertThat(response.getStatus(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("*Projects:*\n>*" + LONG_PROJECT_NAME + "*\n>*Last Build:* #1\n>*Status:* SUCCESS\n\n\n"));
    }

    @Test
    public void testRunNonExistantProject() throws Exception {
        setConfigSettings();
        data.setText("jenkins run project-1");
        JsonResponse response = (JsonResponse) endpoint.doIndex(req);
        assertThat(response.getStatus(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("Could not find project (project-1)\n"));
    }

    @Test
    public void testRunProject() throws Exception {
        setConfigSettings();
        jenkinsRule.createFreeStyleProject(LONG_PROJECT_NAME);
        data.setText("jenkins run " + LONG_PROJECT_NAME);
        JsonResponse response = (JsonResponse) endpoint.doIndex(req);
        assertThat(response.getStatus(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("Build scheduled for project " + LONG_PROJECT_NAME + "\n"));
    }

    @Test
    public void testGetProjectBuildLogWithNonExistantProject() throws Exception {
        setConfigSettings();
        data.setText("jenkins get project_1 #1 log");
        JsonResponse response = (JsonResponse) endpoint.doIndex(req);
        assertThat(response.getStatus(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("Could not find project (project_1)\n"));
    }

    @Test
    public void testGetProjectBuildLog() throws Exception {
        setConfigSettings();
        FreeStyleProject project = jenkinsRule.createFreeStyleProject(LONG_PROJECT_NAME);
        project.scheduleBuild2(0).get();
        data.setText("jenkins get " + LONG_PROJECT_NAME + " #1 log");
        JsonResponse response = (JsonResponse) endpoint.doIndex(req);
        assertThat(response.getStatus(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), containsString("Building in workspace"));
    }

    private void setConfigSettings() {
        GlobalConfig config = GlobalConfiguration.all().get(GlobalConfig.class);
        assert config != null;
        config.setSlackOutgoingWebhookToken("GOOD_TOKEN");
        config.setSlackOutgoingWebhookURL(URL);
    }

    private SlackTextMessage getSlackMessage(JsonResponse response) throws Exception {
        return new ObjectMapper().readValue(response.getJson(),
                SlackTextMessage.class);
    }
}
