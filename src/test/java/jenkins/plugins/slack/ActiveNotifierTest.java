package jenkins.plugins.slack;

import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.model.AbstractBuild;
import hudson.model.CauseAction;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ItemGroup;
import hudson.model.Result;
import java.util.function.Function;
import jenkins.plugins.slack.ActiveNotifier.MessageBuilder;
import jenkins.plugins.slack.logging.BuildAwareLogger;
import jenkins.plugins.slack.matrix.MatrixTriggerMode;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ActiveNotifier.class)
public class ActiveNotifierTest extends TestCase {
    MatrixRun matrixRun = mock(MatrixRun.class);
    FreeStyleBuild freeStyleBuild = mock(FreeStyleBuild.class);
    SlackNotifier slackNotifier = mock(SlackNotifier.class);
    SlackService slack = mock(SlackService.class);

    ActiveNotifier activeNotifier;
    ActiveNotifier freeStyleActiveNotifer;

    @Before
    public void setupActiveNotifier() throws Exception {
        BuildAwareLogger buildAwareLogger = mock(BuildAwareLogger.class);
        TokenExpander tokenExpander = mock(TokenExpander.class);
        MatrixProject matrixProject = mock(MatrixProject.class);
        FreeStyleProject freeStyleProject = mock(FreeStyleProject.class);
        MatrixConfiguration configuration = mock(MatrixConfiguration.class);
        ItemGroup group = mock(ItemGroup.class);
        MatrixRun previousBuild = mock(MatrixRun.class);
        MessageBuilder messageBuilder = mock(MessageBuilder.class);
        Function<AbstractBuild<?, ?>, SlackService> slackFactory = (Function<AbstractBuild<?, ?>, SlackService>) mock(Function.class);

        when(slackNotifier.getNotifyRegression()).thenReturn(true);
        when(slackNotifier.getNotifyFailure()).thenReturn(true);
        when(slackNotifier.getCommitInfoChoice()).thenReturn(CommitInfoChoice.NONE);
        when(slackNotifier.isMatrixRun(freeStyleBuild)).thenReturn(false);
        when(slackNotifier.isMatrixRun(matrixRun)).thenReturn(true);
        when(group.getFullDisplayName()).thenReturn("group");
        doReturn(group).when(matrixProject).getParent();
        when(matrixProject.getLastBuild()).thenReturn(null);
        when(matrixProject.getFullDisplayName()).thenReturn("matrixProject");
        when(previousBuild.getPreviousCompletedBuild()).thenReturn(null);
        when(previousBuild.getResult()).thenReturn(Result.SUCCESS);
        doReturn(matrixProject).when(configuration).getParent();
        when(configuration.getLastBuild()).thenReturn(previousBuild);
        when(configuration.getFullDisplayName()).thenReturn("matrixProject");
        when(matrixRun.getAction(CauseAction.class)).thenReturn(null);
        when(matrixRun.getProject()).thenReturn(configuration);
        when(matrixRun.hasChangeSetComputed()).thenReturn(false);
        when(matrixRun.getNumber()).thenReturn(1);
        when(matrixRun.getResult()).thenReturn(Result.FAILURE);

        doReturn(freeStyleProject).when(freeStyleBuild).getParent();
        doReturn(group).when(freeStyleProject).getParent();
        when(freeStyleProject.getLastBuild()).thenReturn(null);
        when(freeStyleProject.getFullDisplayName()).thenReturn("freeStyleProject");
        when(slackFactory.apply(matrixRun)).thenReturn(slack);
        when(slackFactory.apply(freeStyleBuild)).thenReturn(slack);
        when(messageBuilder.toString()).thenReturn("build status message");
        PowerMockito.whenNew(MessageBuilder.class)
                .withArguments(slackNotifier, matrixRun, buildAwareLogger, tokenExpander)
                .thenReturn(messageBuilder);
        activeNotifier = new ActiveNotifier(slackNotifier, slackFactory, buildAwareLogger, tokenExpander);
        PowerMockito.whenNew(MessageBuilder.class)
            .withArguments(slackNotifier, freeStyleBuild, buildAwareLogger, tokenExpander)
            .thenReturn(messageBuilder);
        freeStyleActiveNotifer = activeNotifier;
    }

    @Test
    public void startedNotifiesMatrixRunParentOnly() {
        when(slackNotifier.getMatrixTriggerMode()).thenReturn(MatrixTriggerMode.ONLY_PARENT);

        activeNotifier.started(matrixRun);

        verify(slack, never()).publish(any(), any());
    }

    @Test
    public void startedNotifiesMatrixRunConfigurationsOnly() {
        when(slackNotifier.getMatrixTriggerMode()).thenReturn(MatrixTriggerMode.ONLY_CONFIGURATIONS);

        activeNotifier.started(matrixRun);

        verify(slack).publish("build status message", "good");
    }

    @Test
    public void startedNotifiesMatrixRunParentAndConfigurations() {
        when(slackNotifier.getMatrixTriggerMode()).thenReturn(MatrixTriggerMode.BOTH);

        activeNotifier.started(matrixRun);

        verify(slack).publish("build status message", "good");
    }

    @Test
    public void finalizedNotifiesMatrixRunParentOnly() {
        when(slackNotifier.getMatrixTriggerMode()).thenReturn(MatrixTriggerMode.ONLY_PARENT);

        activeNotifier.finalized(matrixRun);

        verify(slack, never()).publish(any(), any());
    }

    @Test
    public void finalizedNotifiesMatrixRunConfigurationsOnly() {
        when(slackNotifier.getMatrixTriggerMode()).thenReturn(MatrixTriggerMode.ONLY_CONFIGURATIONS);

        activeNotifier.finalized(matrixRun);

        verify(slack).publish("build status message", "danger");
    }

    @Test
    public void finalizedNotifiesMatrixRunParentAndConfigurations() {
        when(slackNotifier.getMatrixTriggerMode()).thenReturn(MatrixTriggerMode.BOTH);

        activeNotifier.finalized(matrixRun);

        verify(slack).publish("build status message", "danger");
    }

    @Test
    public void completedNotifiesMatrixRunParentOnly() {
        when(slackNotifier.getMatrixTriggerMode()).thenReturn(MatrixTriggerMode.ONLY_PARENT);

        activeNotifier.completed(matrixRun);

        verify(slack, never()).publish(any(), any());
    }

    @Test
    public void completedNotifiesMatrixRunConfigurationsOnly() {
        when(slackNotifier.getMatrixTriggerMode()).thenReturn(MatrixTriggerMode.ONLY_CONFIGURATIONS);

        activeNotifier.completed(matrixRun);

        verify(slack).publish("build status message", "danger");
    }

    @Test
    public void completedNotifiesMatrixRunParentAndConfigurations() {
        when(slackNotifier.getMatrixTriggerMode()).thenReturn(MatrixTriggerMode.BOTH);

        activeNotifier.completed(matrixRun);

        verify(slack).publish("build status message", "danger");
    }

    @Test
    public void startedNotifiesMatrixTriggerModeNull() {
        when(slackNotifier.getMatrixTriggerMode()).thenReturn(null);

        activeNotifier.started(matrixRun);

        verify(slack, never()).publish(any(), any());
    }

    @Test
    public void startedNotifiesFreeStyle() {
        when(slackNotifier.getMatrixTriggerMode()).thenReturn(null);

        freeStyleActiveNotifer.started(freeStyleBuild);

        verify(slack).publish("build status message", "good");
    }

    @Test
    public void startedNotifiesFreeStyleWithMatrixTriggerMode() {
        when(slackNotifier.getMatrixTriggerMode()).thenReturn(MatrixTriggerMode.BOTH);

        freeStyleActiveNotifer.started(freeStyleBuild);

        verify(slack).publish("build status message", "good");
    }
}
