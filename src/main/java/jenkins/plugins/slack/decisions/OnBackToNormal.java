package jenkins.plugins.slack.decisions;

import hudson.model.Result;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.logging.BuildAwareLogger;

public class OnBackToNormal implements Condition {
    private final SlackNotifier preferences;
    private final BuildAwareLogger log;

    public OnBackToNormal(SlackNotifier preferences, BuildAwareLogger log) {
        this.preferences = preferences;
        this.log = log;
    }

    @Override
    public boolean isMetBy(Context context) {
        Result previousResult = context.previousResultOrSuccess();
        return context.currentResult() == Result.SUCCESS
                && (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE);
    }

    @Override
    public boolean userPreferenceMatches() {
        return preferences.getNotifyBackToNormal();
    }

    @Override
    public BuildAwareLogger log() {
        return log;
    }
}
