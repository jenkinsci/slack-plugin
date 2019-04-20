package jenkins.plugins.slack.decisions;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.logging.BuildAwareLogger;

public class NotificationConditions implements Predicate<Context> {
    private final List<Predicate<Context>> conditions;

    public NotificationConditions(List<Predicate<Context>> conditions) {
        this.conditions = conditions;
    }

    public static NotificationConditions create(SlackNotifier preferences, BuildAwareLogger log) {
        return new NotificationConditions(Arrays.asList(
                new OnAborted(preferences, log),
                new OnEveryFailure(preferences, log),
                new OnSingleFailure(preferences, log),
                new OnRepeatedFailure(preferences, log),
                new OnNotBuilt(preferences, log),
                new OnBackToNormal(preferences, log),
                new OnSuccess(preferences, log),
                new OnUnstable(preferences, log)
        ));
    }

    @Override
    public boolean test(Context context) {
        return conditions.stream().anyMatch(p -> p.test(context));
    }
}
