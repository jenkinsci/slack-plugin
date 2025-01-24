package jenkins.plugins.slack;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import jenkins.plugins.slack.logging.BuildAwareLogger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageBuilderEscapeTest {

    private static ActiveNotifier.MessageBuilder messageBuilder;

    @BeforeAll
    static void setupMessageBuilder() {
        AbstractBuild build = mock(AbstractBuild.class);
        AbstractProject project = mock(AbstractProject.class);
        AbstractProject job = mock(AbstractProject.class);
        SlackNotifier notifier = mock(SlackNotifier.class);
        BuildAwareLogger logger = mock(BuildAwareLogger.class);
        TokenExpander tokenExpander = mock(TokenExpander.class);

        when(build.getDisplayName()).thenReturn("build");
        when(build.getProject()).thenReturn(project);
        when(build.getParent()).thenReturn(job);
        when(project.getFullDisplayName()).thenReturn("project");

        messageBuilder = new ActiveNotifier.MessageBuilder(notifier, build, logger, tokenExpander);
    }

    @Test
    void testEscapeAnchor() {
        String input = "<a href='target'>test</a>";
        String expected = "<'target'|test>";
        String escaped = messageBuilder.escape(input);
        assertEquals(expected, escaped);
    }

    @Test
    void testEscapePercent() {
        String input = "hello % world";
        String expected = "hello % world";
        String escaped = messageBuilder.escape(input);
        assertEquals(expected, escaped);
    }

    @Test
    void testEscapeBraces() {
        String input = "something { is } odd";
        String expected = "something { is } odd";
        String escaped = messageBuilder.escape(input);
        assertEquals(expected, escaped);
    }

    @Test
    void testEscapeBracesInLink() {
        String input = "<a href='target'>test { case }</a>";
        String expected = "<'target'|test { case }>";
        String escaped = messageBuilder.escape(input);
        assertEquals(expected, escaped);
    }
}
