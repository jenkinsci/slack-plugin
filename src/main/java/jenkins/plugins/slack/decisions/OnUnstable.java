package jenkins.plugins.slack.decisions;

import hudson.model.Result;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.logging.BuildAwareLogger;

public class OnUnstable implements Condition {
    private final SlackNotifier preferences;
    private final BuildAwareLogger log;

    public OnUnstable(SlackNotifier preferences, BuildAwareLogger log) {
        this.preferences = preferences;
        this.log = log;
    }

    @Override
    public boolean isMetBy(Context context) {
        return context.currentResult() == Result.UNSTABLE;
    }

    @Override
    public boolean userPreferenceMatches() {
        return preferences.getNotifyUnstable();
    }

    @Override
    public BuildAwareLogger log() {
        return log;
    }
}
