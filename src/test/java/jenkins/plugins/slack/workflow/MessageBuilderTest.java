package jenkins.plugins.slack.workflow;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import jenkins.plugins.slack.ActiveNotifier;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

@RunWith(Parameterized.class)
public class MessageBuilderTest extends TestCase {

    private ActiveNotifier.MessageBuilder messageBuilder;
    private String expectedResult;
    private FreeStyleBuild build;


    @Before
    @Override
    public void setUp() throws IOException, ExecutionException, InterruptedException {
        messageBuilder = new ActiveNotifier.MessageBuilder(null, build);
    }

    public MessageBuilderTest(String projectDisplayName, String buildDisplayName, String expectedResult) {
        this.build = Mockito.mock(FreeStyleBuild.class);
        FreeStyleProject project = Mockito.mock(FreeStyleProject.class);

        Mockito.when(build.getProject()).thenReturn(project);
        Mockito.when(build.getDisplayName()).thenReturn(buildDisplayName);

        ItemGroup ig = Mockito.mock(ItemGroup.class);
        Mockito.when(ig.getFullDisplayName()).thenReturn("");
        Mockito.when(project.getParent()).thenReturn(ig);
        Mockito.when(project.getDisplayName()).thenReturn(projectDisplayName);

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
