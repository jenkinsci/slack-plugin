package jenkins.plugins.slack;

import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;
import jenkins.plugins.slack.matrix.MatrixTriggerMode;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SlackNotifierUnitTest {
    SlackNotifier slackNotifier = new SlackNotifier(CommitInfoChoice.AUTHORS);
    MatrixBuild matrixBuild = mock(MatrixBuild.class);
    Launcher launcher = mock(Launcher.class);
    BuildListener buildListener = mock(BuildListener.class);

    @Test
    public void isAnyCustomMessagePopulatedReturnsFalseWhenNonePopulated() {
        assertFalse(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    public void isAnyCustomMessagePopulatedReturnsTrueWhenOnePopulated() {
        slackNotifier.setCustomMessage("hi");

        assertTrue(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    public void isAnyCustomMessagePopulatedReturnsTrueWhenMultiplePopulated() {
        slackNotifier.setCustomMessage("hi");
        slackNotifier.setCustomMessageFailure("hii");

        assertTrue(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    public void createAggregatorNotifiesMatrixBuildParentOnly() throws IOException, InterruptedException {
        SlackNotifier spySlackNotifier = spy(slackNotifier);
        spySlackNotifier.setMatrixTriggerMode(MatrixTriggerMode.ONLY_PARENT);
        doReturn(true).when(spySlackNotifier).perform(matrixBuild, launcher, buildListener);
        MatrixAggregator aggregator = spySlackNotifier.createAggregator(matrixBuild, launcher, buildListener);

        aggregator.startBuild();

        verify(spySlackNotifier).perform(matrixBuild, launcher, buildListener);

        aggregator.endBuild();

        verify(spySlackNotifier, times(2)).perform(matrixBuild, launcher, buildListener);
    }

    @Test
    public void createAggregatorDoesNotNotifyMatrixBuildConfigurationOnly() throws IOException, InterruptedException {
        SlackNotifier spySlackNotifier = spy(slackNotifier);
        spySlackNotifier.setMatrixTriggerMode(MatrixTriggerMode.ONLY_CONFIGURATIONS);
        doReturn(true).when(spySlackNotifier).perform(matrixBuild, launcher, buildListener);
        MatrixAggregator aggregator = spySlackNotifier.createAggregator(matrixBuild, launcher, buildListener);

        aggregator.startBuild();

        aggregator.endBuild();

        verify(spySlackNotifier, never()).perform(matrixBuild, launcher, buildListener);
    }

    @Test
    public void createAggregatorNotifiesMatrixBuildBoth() throws IOException, InterruptedException {
        SlackNotifier spySlackNotifier = spy(slackNotifier);
        spySlackNotifier.setMatrixTriggerMode(MatrixTriggerMode.BOTH);
        doReturn(true).when(spySlackNotifier).perform(matrixBuild, launcher, buildListener);
        MatrixAggregator aggregator = spySlackNotifier.createAggregator(matrixBuild, launcher, buildListener);

        aggregator.startBuild();

        verify(spySlackNotifier).perform(matrixBuild, launcher, buildListener);

        aggregator.endBuild();

        verify(spySlackNotifier, times(2)).perform(matrixBuild, launcher, buildListener);
    }
}
