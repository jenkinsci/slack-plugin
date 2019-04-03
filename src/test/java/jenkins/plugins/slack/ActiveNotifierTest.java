package jenkins.plugins.slack;

import hudson.matrix.MatrixConfiguration;
import hudson.model.AbstractBuild;
import hudson.model.CauseAction;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.model.ItemGroup;
import hudson.model.Result;
import junit.framework.TestCase;
import jenkins.plugins.slack.logging.BuildAwareLogger;
import jenkins.plugins.slack.matrix.MatrixTriggerMode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.function.Function;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ActiveNotifier.class)
public class ActiveNotifierTest extends TestCase {
  MatrixRun matrixRun = mock(MatrixRun.class);
  SlackNotifier slackNotifier = mock(SlackNotifier.class);
  SlackService slack = mock(SlackService.class);

  ActiveNotifier activeNotifier;

  @Before
  public void setupActiveNotifier() throws Exception {
    BuildAwareLogger buildAwareLogger = mock(BuildAwareLogger.class);
    TokenExpander tokenExpander = mock(TokenExpander.class);
    MatrixProject project = mock(MatrixProject.class);
    MatrixConfiguration configuration = mock(MatrixConfiguration.class);
    ItemGroup group = mock(ItemGroup.class);
    MatrixRun previousBuild = mock(MatrixRun.class);
    ActiveNotifier.MessageBuilder messageBuilder = mock(ActiveNotifier.MessageBuilder.class);
    Function<AbstractBuild<?, ?>, SlackService> slackFactory = (Function<AbstractBuild<?, ?>, SlackService>) mock(Function.class);

    when(slackNotifier.getNotifyRegression()).thenReturn(true);
    when(slackNotifier.getNotifyFailure()).thenReturn(true);
    when(slackNotifier.getCommitInfoChoice()).thenReturn(CommitInfoChoice.NONE);
    when(group.getFullDisplayName()).thenReturn("group");
    when(project.getParent()).thenReturn(group);
    when(project.getLastBuild()).thenReturn(null);
    when(project.getFullDisplayName()).thenReturn("project");
    when(previousBuild.getPreviousCompletedBuild()).thenReturn(null);
    when(previousBuild.getResult()).thenReturn(Result.SUCCESS);
    when(configuration.getParent()).thenReturn(project);
    when(configuration.getLastBuild()).thenReturn(previousBuild);
    when(configuration.getFullDisplayName()).thenReturn("project");
    when(matrixRun.getAction(CauseAction.class)).thenReturn(null);
    when(matrixRun.getProject()).thenReturn(configuration);
    when(matrixRun.hasChangeSetComputed()).thenReturn(false);
    when(matrixRun.getNumber()).thenReturn(1);
    when(matrixRun.getResult()).thenReturn(Result.FAILURE);
    when(slackFactory.apply(matrixRun)).thenReturn(slack);
    when(messageBuilder.toString()).thenReturn("build status message");
    PowerMockito.whenNew(ActiveNotifier.MessageBuilder.class)
            .withArguments(slackNotifier, matrixRun, buildAwareLogger, tokenExpander)
            .thenReturn(messageBuilder);
    activeNotifier = new ActiveNotifier(slackNotifier, slackFactory, buildAwareLogger, tokenExpander);
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
}
