package jenkins.plugins.slack.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import hudson.matrix.MatrixProject;
import hudson.model.FreeStyleProject;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.CommitInfoChoice;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.SlackNotifier.SlackJobProperty;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;

@SuppressWarnings("deprecation")
public class BackwordsCompatible_1_8_SlackNotifierTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    public Jenkins jenkins;

    @Before
    public void setup() {
        jenkins = j.getInstance();
    }

    @Test
    @LocalData
    public void testBasicMigration() {
        FreeStyleProject project = (FreeStyleProject) jenkins.getItem("Test_Slack_Plugin");
        SlackNotifier notifier = project.getPublishersList().get(SlackNotifier.class);

        assertEquals("jenkins-slack-plugin", notifier.getTeamDomain());
        assertEquals("auth-token-for-test", notifier.getAuthToken());
        assertEquals("http://localhost:8080/", notifier.getBuildServerUrl());
        assertEquals("#slack-plugin-testing", notifier.getRoom());

        assertFalse(notifier.getStartNotification());
        assertTrue(notifier.getNotifySuccess());
        assertFalse(notifier.getNotifyAborted());
        assertFalse(notifier.getNotifyNotBuilt());
        assertFalse(notifier.getNotifyUnstable());
        assertTrue(notifier.getNotifyFailure());
        assertFalse(notifier.getNotifyBackToNormal());
        assertFalse(notifier.getNotifyRepeatedFailure());
        assertFalse(notifier.includeTestSummary());
        assertEquals(CommitInfoChoice.NONE, notifier.getCommitInfoChoice());
        assertFalse(notifier.includeCustomMessage());
        assertEquals("", notifier.getCustomMessage());

        assertNull(project.getProperty(SlackJobProperty.class));
    }

    @Test
    @LocalData
    public void testGlobalSettingsOverriden() {
        FreeStyleProject project = (FreeStyleProject) jenkins.getItem("Test_Slack_Plugin");
        SlackNotifier notifier = project.getPublishersList().get(SlackNotifier.class);

        assertEquals("jenkins-slack-plugin", notifier.getTeamDomain());
        assertEquals("auth-token-for-test", notifier.getAuthToken());
        assertEquals("http://localhost:8080/", notifier.getBuildServerUrl());
        assertEquals("#slack-plugin-testing", notifier.getRoom());

        assertFalse(notifier.getStartNotification());
        assertTrue(notifier.getNotifySuccess());
        assertFalse(notifier.getNotifyAborted());
        assertFalse(notifier.getNotifyNotBuilt());
        assertFalse(notifier.getNotifyUnstable());
        assertTrue(notifier.getNotifyFailure());
        assertFalse(notifier.getNotifyBackToNormal());
        assertFalse(notifier.getNotifyRepeatedFailure());
        assertFalse(notifier.includeTestSummary());
        assertEquals(CommitInfoChoice.NONE, notifier.getCommitInfoChoice());
        assertFalse(notifier.includeCustomMessage());
        assertEquals("", notifier.getCustomMessage());

        assertNull(project.getProperty(SlackJobProperty.class));
    }

    @Test
    @LocalData
    public void testGlobalSettingsNotOverridden() throws IOException {
        FreeStyleProject project = (FreeStyleProject) jenkins.getItem("Test_Slack_Plugin");
        SlackNotifier notifier = project.getPublishersList().get(SlackNotifier.class);

        assertEquals("", notifier.getTeamDomain());
        assertEquals("", notifier.getAuthToken());
        assertEquals(j.getURL().toString(), notifier.getBuildServerUrl());
        assertEquals("", notifier.getRoom());

        assertFalse(notifier.getStartNotification());
        assertTrue(notifier.getNotifySuccess());
        assertFalse(notifier.getNotifyAborted());
        assertFalse(notifier.getNotifyNotBuilt());
        assertFalse(notifier.getNotifyUnstable());
        assertTrue(notifier.getNotifyFailure());
        assertFalse(notifier.getNotifyBackToNormal());
        assertFalse(notifier.getNotifyRepeatedFailure());
        assertFalse(notifier.includeTestSummary());
        assertEquals(CommitInfoChoice.NONE, notifier.getCommitInfoChoice());
        assertFalse(notifier.includeCustomMessage());
        assertEquals("", notifier.getCustomMessage());

        assertNull(project.getProperty(SlackJobProperty.class));
    }

    @Test
    @LocalData
    public void testMigrationFromNoPlugin() {
        FreeStyleProject project1 = (FreeStyleProject) jenkins.getItem("Test_01");
        assertNull(project1.getPublishersList().get(SlackNotifier.class));
        assertNull(project1.getProperty(SlackJobProperty.class));

        FreeStyleProject project2 = (FreeStyleProject) jenkins.getItem("Test_02");
        assertNull(project2.getPublishersList().get(SlackNotifier.class));
        assertNull(project2.getProperty(SlackJobProperty.class));

        MatrixProject project3 = (MatrixProject) jenkins.getItem("Test_03");
        assertNull(project3.getPublishersList().get(SlackNotifier.class));
        assertNull(project3.getProperty(SlackJobProperty.class));
    }

    @Test
    @LocalData
    public void testMigrationOfSomeJobs() throws IOException {
        // Project without Slack notifications
        FreeStyleProject project1 = (FreeStyleProject) jenkins.getItem("Test_01");
        assertNull(project1.getPublishersList().get(SlackNotifier.class));
        assertNull(project1.getProperty(SlackJobProperty.class));

        // Another project without Slack notifications
        MatrixProject project3 = (MatrixProject) jenkins.getItem("Test_03");
        assertNull(project3.getPublishersList().get(SlackNotifier.class));
        assertNull(project3.getProperty(SlackJobProperty.class));

        // Project with Slack notifications
        FreeStyleProject project2 = (FreeStyleProject) jenkins.getItem("Test_02");
        SlackNotifier notifier = project2.getPublishersList().get(SlackNotifier.class);
        assertNotNull(notifier);

        assertEquals("", notifier.getTeamDomain());
        assertEquals("", notifier.getAuthToken());
        assertEquals(j.getURL().toString(), notifier.getBuildServerUrl());
        assertEquals("", notifier.getRoom());

        assertTrue(notifier.getStartNotification());
        assertTrue(notifier.getNotifySuccess());
        assertTrue(notifier.getNotifyAborted());
        assertTrue(notifier.getNotifyNotBuilt());
        assertTrue(notifier.getNotifyUnstable());
        assertTrue(notifier.getNotifyFailure());
        assertTrue(notifier.getNotifyBackToNormal());
        assertTrue(notifier.getNotifyRepeatedFailure());
        assertTrue(notifier.includeTestSummary());
        assertEquals(CommitInfoChoice.AUTHORS_AND_TITLES, notifier.getCommitInfoChoice());
        assertTrue(notifier.includeCustomMessage());
        assertEquals("Custom message for 1.8 plugin.", notifier.getCustomMessage());

        assertNull(project2.getProperty(SlackJobProperty.class));
    }

    @Test
    @LocalData
    public void testMigrationWithNoNotifier() {
        FreeStyleProject project = (FreeStyleProject) jenkins.getItem("Test_01");
        assertNull(project.getPublishersList().get(SlackNotifier.class));
        assertNull(project.getProperty(SlackJobProperty.class));
    }
}
