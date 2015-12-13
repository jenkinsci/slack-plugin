package jenkins.plugins.slack.workflow;


import hudson.model.Result;
import jenkins.plugins.slack.Messages;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.StepConfigTester;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;
import org.jvnet.hudson.test.RestartableJenkinsRule;

public class SlackSendStepTest {
    @Rule
    public RestartableJenkinsRule story = new RestartableJenkinsRule();

    @Test
    public void configRoundTrip() throws Exception {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                SlackSendStep step1 = new SlackSendStep("message");
                step1.color = "good";
                step1.channel = "#channel";

                SlackSendStep step2 = new StepConfigTester(story.j).configRoundTrip(step1);
                story.j.assertEqualDataBoundBeans(step1, step2);
            }
        });
    }

    @Test
    public void test_publish_message() throws Exception {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "workflow");
                //just define message
                job.setDefinition(new CpsFlowDefinition("slackSend 'message';", true));
                WorkflowRun run = story.j.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
                //everything should come from global configuration
                story.j.assertLogContains(Messages.SlackSendStepConfig(true, true, true, true), run);
            }
        });
    }

    @Test
    public void test_global_config_override() throws Exception {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "workflow");
                //just define message
                job.setDefinition(new CpsFlowDefinition("slackSend(message: 'message', teamDomain: 'teamDomain', token: 'token', channel: '#channel', color: 'good');", true));
                WorkflowRun run = story.j.assertBuildStatusSuccess(job.scheduleBuild2(0).get());
                //everything should come from step configuration
                story.j.assertLogContains(Messages.SlackSendStepConfig(false, false, false, false), run);
            }
        });
    }

    @Test
    public void test_fail_on_error() throws Exception {
        story.addStep(new Statement() {
            @Override
            public void evaluate() throws Throwable {
                WorkflowJob job = story.j.jenkins.createProject(WorkflowJob.class, "workflow");
                //just define message
                job.setDefinition(new CpsFlowDefinition("slackSend(message: 'message', teamDomain: 'teamDomain', token: 'token', channel: '#channel', color: 'good', failOnError: true);", true));
                WorkflowRun run = story.j.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());
                //everything should come from step configuration
                story.j.assertLogContains(Messages.NotificationFailed(), run);
            }
        });
    }
}
