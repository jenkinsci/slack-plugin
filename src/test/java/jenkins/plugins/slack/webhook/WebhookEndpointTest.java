package jenkins.plugins.slack.webhook;


import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.containsString;

import static org.junit.Assert.assertThat;

import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebRequest;
import static com.gargoylesoftware.htmlunit.HttpMethod.POST;

import com.gargoylesoftware.htmlunit.html.HtmlForm;

import org.jvnet.hudson.test.JenkinsRule;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

import com.gargoylesoftware.htmlunit.util.NameValuePair;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

import hudson.model.FreeStyleProject;
import jenkins.model.GlobalConfiguration;

import jenkins.plugins.slack.webhook.model.SlackTextMessage;

public class WebhookEndpointTest {

    private JenkinsRule.WebClient client;

    private static final String URL = "webook";
    private static final String ENDPOINT = URL + "/";
    private static final String LONG_PROJECT_NAME = "slack_plugin";

    private List<NameValuePair> data;

    @Rule
    public final JenkinsRule jenkinsRule = new JenkinsRule();

    
    @Before
    public void setUp() {
        client = jenkinsRule.createWebClient();
        data = new ArrayList<>();
        data.add(new NameValuePair("token", "GOOD_TOKEN"));
        data.add(new NameValuePair("trigger_word", "jenkins")); 
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
        WebResponse response = makeRequest(null);
        assertThat(response.getStatusCode(), is(HTTP_NOT_FOUND));
    }

    @Test
    public void testUnconfiguredSlackToken() throws Exception {
        HtmlForm form = jenkinsRule.createWebClient().goTo("configure").getFormByName("config");
        form.getInputByName("_.slackOutgoingWebhookURL").setValueAttribute(URL);
        jenkinsRule.submit(form);

        WebResponse response = makeRequest(null);

        assertThat(response.getStatusCode(), is(HTTP_OK));
        assertThat(getSlackMessage(response).getText(), is("Slack token not set"));
    }

    @Test
    public void testNoTextPostData() throws Exception {
        setConfigSettings();
        List<NameValuePair> goodToken = new ArrayList<>();
        goodToken.add(new NameValuePair("token", "GOOD_TOKEN"));

        WebResponse response = makeRequest(goodToken);
        assertThat(getSlackMessage(response).getText(), is("Invalid command, text field required"));
    } 

    @Test
    public void testNoTriggerWordPostData() throws Exception {
        // No trigger word is present, which is the case when Slack "slash commands" are used
        setConfigSettings();
        List<NameValuePair> goodToken = new ArrayList<>();
        goodToken.add(new NameValuePair("token", "GOOD_TOKEN"));
        goodToken.add(new NameValuePair("text", "list projects"));

        WebResponse response = makeRequest(goodToken);
        assertThat(getSlackMessage(response).getText(), is("*Projects:*\n>_No projects found_"));
    }

    @Test
    public void testInvalidConfiguredSlackToken() throws Exception {
        GlobalConfig config = GlobalConfiguration.all().get(GlobalConfig.class);        
        assertThat(config.getSlackOutgoingWebhookToken(), is(nullValue()));

        setConfigSettings();

        List<NameValuePair> badData = new ArrayList<>();
        badData.add(new NameValuePair("token", "BAD_TOKEN"));

        WebResponse response = makeRequest(badData);
        
        assertThat(getSlackMessage(response).getText(), is("Invalid Slack token"));
    }

    @Test
    public void testInvalidTriggerWord() throws Exception {
        setConfigSettings();
        data.add(new NameValuePair("text", "jenkinns list projects"));
        WebResponse response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("Invalid command, invalid trigger_word"));
    }

    @Test
    public void testListProjects() throws Exception {
        setConfigSettings(); 
        data.add(new NameValuePair("text", "jenkins list projects")); 
        WebResponse response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("*Projects:*\n>_No projects found_"));

        FreeStyleProject project = jenkinsRule.createFreeStyleProject(LONG_PROJECT_NAME);
        response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("*Projects:*\n>*"+LONG_PROJECT_NAME+"*\n>*Last Build:* #TBD\n>*Status:* TBD\n\n\n"));

        project.scheduleBuild2(0).get();
        response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("*Projects:*\n>*"+LONG_PROJECT_NAME+"*\n>*Last Build:* #1\n>*Status:* SUCCESS\n\n\n"));
    }

    @Test
    public void testRunNonExistantProject() throws Exception {
        setConfigSettings();
        data.add(new NameValuePair("text", "jenkins run project-1"));
        WebResponse response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("Could not find project (project-1)\n"));
    }

    @Test
    public void testRunProject() throws Exception {
        setConfigSettings();
        jenkinsRule.createFreeStyleProject(LONG_PROJECT_NAME);
        data.add(new NameValuePair("text", "jenkins run "+LONG_PROJECT_NAME));
        WebResponse response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("Build scheduled for project "+LONG_PROJECT_NAME+"\n"));
    }

    @Test
    public void testGetProjectBuildLogWithNonExistantProject() throws Exception {
        setConfigSettings();
        data.add(new NameValuePair("text", "jenkins get project_1 #1 log"));
        WebResponse response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), is("Could not find project (project_1)\n"));
    }

    @Test
    public void testGetProjectBuildLog() throws Exception {
        setConfigSettings();
        FreeStyleProject project = jenkinsRule.createFreeStyleProject(LONG_PROJECT_NAME);
        project.scheduleBuild2(0).get();
        data.add(new NameValuePair("text", "jenkins get "+LONG_PROJECT_NAME+" #1 log"));
        WebResponse response = makeRequest(data);
        assertThat(getSlackMessage(response).getText(), containsString("Building in workspace"));
    }

    private void setConfigSettings() throws Exception {
        HtmlForm form = jenkinsRule.createWebClient().goTo("configure").getFormByName("config");
        form.getInputByName("_.slackOutgoingWebhookURL").setValueAttribute(URL);
        form.getInputByName("_.slackOutgoingWebhookToken").setValueAttribute("GOOD_TOKEN");
        jenkinsRule.submit(form);
    }

    private SlackTextMessage getSlackMessage(WebResponse response) throws Exception { 
        return new ObjectMapper().readValue(response.getContentAsString(),
            SlackTextMessage.class);
    }

    private WebResponse makeRequest(List<NameValuePair> postData) throws Exception {
        WebRequest request =
            new WebRequest(client.createCrumbedUrl(ENDPOINT), POST);

        if (postData != null)
            request.setRequestParameters(postData);

        request.setCharset(StandardCharsets.UTF_8);

        return client.loadWebResponse(request);
    } 
}
