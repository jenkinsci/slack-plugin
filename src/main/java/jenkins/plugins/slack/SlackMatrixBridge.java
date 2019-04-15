package jenkins.plugins.slack;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.MatrixAggregatable;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;
import jenkins.plugins.slack.matrix.MatrixTriggerMode;

@Extension(optional = true)
public class SlackMatrixBridge implements MatrixAggregatable {

    public MatrixAggregator createAggregator(MatrixBuild matrixBuild, Launcher launcher, BuildListener buildListener) {
        return new MatrixAggregator(matrixBuild, launcher, buildListener) {

            private boolean slackNotify() {
                SlackNotifier slackNotifier = build
                    .getParent()
                    .getPublishersList()
                    .get(SlackNotifier.class);

                if (slackNotifier != null) {
                    MatrixTriggerMode matrixTriggerMode = slackNotifier.getMatrixTriggerMode();
                    if (matrixTriggerMode != null && matrixTriggerMode.forParent) {
                        return slackNotifier.perform(build, launcher, listener);
                    }
                }
                return true;
            }

            @Override
            public boolean startBuild() {
                return slackNotify();
            }

            @Override
            public boolean endBuild() {
                return slackNotify();
            }
        };
    }
}
