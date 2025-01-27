package jenkins.plugins.slack.workflow;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import jenkins.plugins.slack.ActiveNotifier;
import jenkins.plugins.slack.TokenExpander;
import jenkins.plugins.slack.logging.BuildAwareLogger;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MessageBuilderTest {

    static Object[][] businessTypeKeys() {
        return new Object[][]{
                {"", "", " -  "},
                {"project", "#43 Started by changes from Bob", "project - #43 Started by changes from Bob "},
                {"project", "#541 <a href=\"https://bitbucket.org/org/project/pull-request/125\">#125 Bug</a>",
                        "project - #541 <https://bitbucket.org/org/project/pull-request/125|#125 Bug> "},
                {"project", "#541 <b>Bold Project</b>", "project - #541 &lt;b&gt;Bold Project&lt;/b&gt; "},
                {"project", "#541 <a no-url>bob</a>", "project - #541 &lt;a no-url&gt;bob&lt;/a&gt; "}
        };
    }

    @ParameterizedTest
    @MethodSource("businessTypeKeys")
    void testStartMessage(String projectDisplayName, String buildDisplayName, String expectedResult) {
        FreeStyleBuild build = mock(FreeStyleBuild.class);
        FreeStyleProject project = mock(FreeStyleProject.class);

        when(build.getProject()).thenReturn(project);
        when(build.getDisplayName()).thenReturn(buildDisplayName);

        when(project.getFullDisplayName()).thenReturn(projectDisplayName);
        when(project.getDisplayName()).thenReturn(projectDisplayName);

        ActiveNotifier.MessageBuilder messageBuilder =
                new ActiveNotifier.MessageBuilder(null, build, mock(BuildAwareLogger.class), mock(TokenExpander.class));

        assertEquals(expectedResult, messageBuilder.toString());
    }

}
