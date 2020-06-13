package jenkins.plugins.slack.decisions;

import hudson.model.Result;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.logging.BuildAwareLogger;

public class OnAborted implements Condition {
    private final SlackNotifier preferences;
    private final BuildAwareLogger log;

    public OnAborted(SlackNotifier preferences, BuildAwareLogger log) {
        this.preferences = preferences;
        this.log = log;
    }

    @Override
    public boolean isMetBy(Context context) {
        return context.currentResult() == Result.ABORTED;
    }

    @Override
    public boolean userPreferenceMatches() {
        return preferences.getNotifyAborted();
    }

    @Override
    public BuildAwareLogger log() {
        return log;
    }
}
