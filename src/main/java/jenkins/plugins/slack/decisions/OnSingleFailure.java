package jenkins.plugins.slack.decisions;

import hudson.model.Result;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.logging.BuildAwareLogger;

public class OnSingleFailure implements Condition {
    private final SlackNotifier preferences;
    private final BuildAwareLogger log;

    public OnSingleFailure(SlackNotifier preferences, BuildAwareLogger log) {
        this.preferences = preferences;
        this.log = log;
    }

    @Override
    public boolean isMetBy(Context context) {
        return context.currentResult() == Result.FAILURE //notify only on single failed build
                && context.previousResultOrSuccess() != Result.FAILURE;
    }

    @Override
    public boolean userPreferenceMatches() {
        return preferences.getNotifyFailure();
    }

    @Override
    public BuildAwareLogger log() {
        return log;
    }
}
