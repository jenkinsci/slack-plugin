package jenkins.plugins.slack;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;

@Extension(optional = true)
public class SlackMatrixBridge implements MatrixAggregatable {

    public MatrixAggregator createAggregator(MatrixBuild matrixBuild, Launcher launcher, BuildListener buildListener) {
        return new MatrixAggregator(matrixBuild, launcher, buildListener) {
            @Override
            public boolean startBuild() {
                SlackNotifier slackNotifier = build.getParent().getPublishersList().get(SlackNotifier.class);

                if (slackNotifier != null && slackNotifier.getMatrixTriggerMode().forParent) {
                    return slackNotifier.perform(this.build, this.launcher, this.listener);
                }
                return true;
            }

            @Override
            public boolean endBuild() {
                SlackNotifier slackNotifier = build.getParent().getPublishersList().get(SlackNotifier.class);

                if (slackNotifier != null && slackNotifier.getMatrixTriggerMode().forParent) {
                    return slackNotifier.perform(this.build, this.launcher, this.listener);
                }
                return true;
            }
        };
    }
}
