package jenkins.plugins.slack.decisions;

import hudson.model.Result;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.logging.BuildAwareLogger;

public class OnNotBuilt implements Condition {
    private final SlackNotifier preferences;
    private final BuildAwareLogger log;

    public OnNotBuilt(SlackNotifier preferences, BuildAwareLogger log) {
        this.preferences = preferences;
        this.log = log;
    }

    @Override
    public boolean isMetBy(Context context) {
        return context.currentResult() == Result.NOT_BUILT;
    }

    @Override
    public boolean userPreferenceMatches() {
        return preferences.getNotifyNotBuilt();
    }

    @Override
    public BuildAwareLogger log() {
        return log;
    }
}
