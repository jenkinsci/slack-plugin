package jenkins.plugins.slack.workflow;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import java.util.Arrays;
import java.util.Collection;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.ActiveNotifier;
import jenkins.plugins.slack.TokenExpander;
import jenkins.plugins.slack.logging.BuildAwareLogger;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class MessageBuilderTest extends TestCase {

    private ActiveNotifier.MessageBuilder messageBuilder;
    private String expectedResult;
    private FreeStyleBuild build;


    @Before
    @Override
    public void setUp() {
        messageBuilder = new ActiveNotifier.MessageBuilder(null, build, mock(BuildAwareLogger.class), mock(TokenExpander.class));
    }

    public MessageBuilderTest(String projectDisplayName, String buildDisplayName, String expectedResult) {
        this.build = mock(FreeStyleBuild.class);
        FreeStyleProject project = mock(FreeStyleProject.class);

        when(build.getProject()).thenReturn(project);
        when(build.getDisplayName()).thenReturn(buildDisplayName);

        Jenkins jenkins = mock(Jenkins.class);
        when(jenkins.getFullDisplayName()).thenReturn("");

        when(project.getParent()).thenReturn(jenkins);
        when(project.getDisplayName()).thenReturn(projectDisplayName);

        this.expectedResult = expectedResult;

    }

    @Parameterized.Parameters
    public static Collection businessTypeKeys() {
        return Arrays.asList(new Object[][]{
                {"", "", " -  "},
                {"project", "#43 Started by changes from Bob", "project - #43 Started by changes from Bob "},
                {"project", "#541 <a href=\"https://bitbucket.org/org/project/pull-request/125\">#125 Bug</a>",
                        "project - #541 <https://bitbucket.org/org/project/pull-request/125|#125 Bug> "},
                {"project", "#541 <b>Bold Project</b>", "project - #541 &lt;b&gt;Bold Project&lt;/b&gt; "},
                {"project", "#541 <a no-url>bob</a>", "project - #541 &lt;a no-url&gt;bob&lt;/a&gt; "}
        });
    }

    @Test
    public void testStartMessage() {
        assertEquals(expectedResult, messageBuilder.toString());
    }

}
