package jenkins.plugins.slack.pipeline;


import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.SnippetizerTester;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class SlackUploadFileStepIntegrationTest {
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void configRoundTrip() throws Exception {
        SnippetizerTester st = new SnippetizerTester(j);
        SlackUploadFileStep step = new SlackUploadFileStep("file.txt");
        st.assertRoundTrip(step, "slackUploadFile 'file.txt'");
        step.setInitialComment("hi");
        st.assertRoundTrip(step, "slackUploadFile filePath: 'file.txt', initialComment: 'hi'");
        step.setChannel("channel");
        st.assertRoundTrip(step, "slackUploadFile channel: 'channel', filePath: 'file.txt', initialComment: 'hi'");
        step.setCredentialId("cred");
        st.assertRoundTrip(step, "slackUploadFile channel: 'channel', credentialId: 'cred', filePath: 'file.txt', initialComment: 'hi'");
    }

    @Test
    public void test_run_passes() throws Exception {
        Jenkins jenkins = j.jenkins;

        SystemCredentialsProvider instance = SystemCredentialsProvider.getInstance();
        instance.getCredentials().add(new StringCredentialsImpl(CredentialsScope.GLOBAL, "credentialId", "desc", Secret.fromString("blah")));
        instance.save();

        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "workflow");
        //just define message
        job.setDefinition(new CpsFlowDefinition("node { slackUploadFile(initialComment: 'message', credentialId: 'credentialId', channel: '#channel') }", true));
        j.assertBuildStatusSuccess(job.scheduleBuild2(0).get());
    }
}
