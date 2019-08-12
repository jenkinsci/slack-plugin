package jenkins.plugins.slack;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import java.io.IOException;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.logging.BuildAwareLogger;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageBuilderEscapeTest {

    private static ActiveNotifier.MessageBuilder messageBuilder;

    @BeforeClass
    public static void setupMessageBuilder() {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject project = mock(AbstractProject.class);
        AbstractProject job = mock(AbstractProject.class);
        Jenkins jenkins = mock(Jenkins.class);
        SlackNotifier notifier = mock(SlackNotifier.class);
        BuildAwareLogger logger = mock(BuildAwareLogger.class);
        TokenExpander tokenExpander = mock(TokenExpander.class);

        when(build.getDisplayName()).thenReturn("build");
        when(build.getProject()).thenReturn(project);
        when(build.getParent()).thenReturn(job);
        when(job.getParent()).thenReturn(jenkins);
        when(project.getParent()).thenReturn(jenkins);

        when(jenkins.getFullDisplayName()).thenReturn("jenkins");

        messageBuilder = new ActiveNotifier.MessageBuilder(notifier, build, logger, tokenExpander);
    }

    @Test
    public void testEscapeAnchor() throws IOException {
        String input = "<a href='target'>test</a>";
        String expected = "<'target'|test>";
        String escaped = messageBuilder.escape(input);
        assertEquals(expected, escaped);
    }

    @Test
    public void testEscapePercent() throws IOException {
        String input = "hello % world";
        String expected = "hello % world";
        String escaped = messageBuilder.escape(input);
        assertEquals(expected, escaped);
    }

    @Test
    public void testEscapeBraces() throws IOException {
        String input = "something { is } odd";
        String expected = "something { is } odd";
        String escaped = messageBuilder.escape(input);
        assertEquals(expected, escaped);
    }

    @Test
    public void testEscapeBracesInLink() throws IOException {
        String input = "<a href='target'>test { case }</a>";
        String expected = "<'target'|test { case }>";
        String escaped = messageBuilder.escape(input);
        assertEquals(expected, escaped);
    }
}
