package jenkins.plugins.slack.pipeline;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.cps.SnippetizerTester;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class SlackUserIdFromEmailStepIntegrationTest {

    private static final String EMAIL_ADDRESS = "spengler@ghostbusters.example.com";

    @Test
    void configRoundTrip(JenkinsRule j) throws Exception {
        SnippetizerTester st = new SnippetizerTester(j);
        SlackUserIdFromEmailStep step = new SlackUserIdFromEmailStep(EMAIL_ADDRESS);
        st.assertRoundTrip(step, "slackUserIdFromEmail '" + EMAIL_ADDRESS + "'");
    }

    @Test
    void testRunPasses(JenkinsRule j) throws Exception {
        Jenkins jenkins = j.jenkins;

        SystemCredentialsProvider instance = SystemCredentialsProvider.getInstance();
        instance.getCredentials().add(new StringCredentialsImpl(CredentialsScope.GLOBAL, "credentialId", "desc", Secret.fromString("blah")));
        instance.save();

        WorkflowJob job = jenkins.createProject(WorkflowJob.class, "workflow");
        //just define message
        job.setDefinition(new CpsFlowDefinition("node { slackUserIdFromEmail(email: '" + EMAIL_ADDRESS + "', botUser: true) }", true));
        j.assertBuildStatusSuccess(job.scheduleBuild2(0).get());
    }
}
