package jenkins.plugins.slack.extension;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import hudson.model.TaskListener;
import jenkins.model.Jenkins;
import jenkins.plugins.slack.ActiveNotifier;
import jenkins.plugins.slack.SlackNotifier;
import junit.framework.Assert;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import java.io.IOException;
import java.util.List;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.startsWith;

@RunWith(PowerMockRunner.class)
@PrepareForTest(TokenMacro.class)
public class SlackMessageExtensionTest {

    private Jenkins jenkins;
    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() {
        jenkins = j.getInstance();
    }

    @Test
    public void testSlackExtensions() {
        List<SlackMessageExtension> extensions = jenkins.getExtensionList(SlackMessageExtension.class);
        Assert.assertEquals(1, extensions.size());
        Assert.assertEquals(InternalExtension.class, extensions.get(0).getClass());
    }

    @Test
    public void testReplacement() throws IOException, InterruptedException, MacroEvaluationException {
        FreeStyleBuild build = Mockito.mock(FreeStyleBuild.class);
        FreeStyleProject project = Mockito.mock(FreeStyleProject.class);
        SlackNotifier notifier = Mockito.mock(SlackNotifier.class);
        PowerMockito.mockStatic(TokenMacro.class);

        Mockito.when(build.getProject()).thenReturn(project);
        EnvVars vars = Mockito.mock(EnvVars.class);
        Mockito.when(build.getEnvironment(any(TaskListener.class))).thenReturn(vars);
        Mockito.when(build.getDisplayName()).thenReturn("#43 Started by changes from Bob");
        Mockito.when(vars.expand(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return (String) args[0];
            }
        });

        ItemGroup ig = Mockito.mock(ItemGroup.class);
        Mockito.when(ig.getFullDisplayName()).thenReturn("");
        Mockito.when(project.getParent()).thenReturn(ig);
        Mockito.when(project.getDisplayName()).thenReturn("project");
        BDDMockito.when(TokenMacro.expandAll(any(FreeStyleBuild.class),any(TaskListener.class),startsWith("100%"))).thenReturn("100%");

        Mockito.when(notifier.getCustomMessage()).thenReturn("${INTERNAL_STRING}");

        ActiveNotifier.MessageBuilder messageBuilder = new ActiveNotifier.MessageBuilder(notifier, build);
        messageBuilder.appendCustomMessage();

        PowerMockito.verifyStatic();

        String expectedResult = "project - #43 Started by changes from Bob \n100%";

        Assert.assertEquals(expectedResult, messageBuilder.toString());
    }

    @Extension
    public static class InternalExtension extends SlackMessageExtension {
        @Override
        public String doReplacement(String message, AbstractBuild build) {
            return message.replace("${INTERNAL_STRING}","100%");
        }
    }

}
