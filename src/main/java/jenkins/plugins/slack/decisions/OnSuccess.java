package jenkins.plugins.slack.decisions;

import hudson.model.Result;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.logging.BuildAwareLogger;

public class OnSuccess implements Condition {
    private final SlackNotifier preferences;
    private final BuildAwareLogger log;

    public OnSuccess(SlackNotifier preferences, BuildAwareLogger log) {
        this.preferences = preferences;
        this.log = log;
    }

    @Override
    public boolean isMetBy(Context context) {
        return context.currentResult() == Result.SUCCESS;
    }

    @Override
    public boolean userPreferenceMatches() {
        return preferences.getNotifySuccess();
    }

    @Override
    public BuildAwareLogger log() {
        return log;
    }
}
