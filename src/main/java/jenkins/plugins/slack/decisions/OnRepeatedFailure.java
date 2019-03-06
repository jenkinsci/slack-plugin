package jenkins.plugins.slack.decisions;

import hudson.model.Result;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.logging.BuildAwareLogger;

public class OnRepeatedFailure implements Condition {
    private final SlackNotifier preferences;
    private final BuildAwareLogger log;

    public OnRepeatedFailure(SlackNotifier preferences, BuildAwareLogger log) {
        this.preferences = preferences;
        this.log = log;
    }

    @Override
    public boolean isMetBy(Context context) {
        return context.currentResult() == Result.FAILURE //notify only on repeated failures
                && context.previousResultOrSuccess() == Result.FAILURE;
    }

    @Override
    public boolean userPreferenceMatches() {
        return preferences.getNotifyRepeatedFailure();
    }

    @Override
    public BuildAwareLogger log() {
        return log;
    }
}
