package jenkins.plugins.slack.decisions;

import jenkins.plugins.slack.logging.BuildAwareLogger;

import java.util.function.Predicate;

interface Condition extends Predicate<Context> {
    boolean isMetBy(Context context);
    boolean userPreferenceMatches();
    BuildAwareLogger log();

    @Override
    default boolean test(Context context) {
        boolean isMet = isMetBy(context);
        boolean preferences = userPreferenceMatches();
        if (isMet) {
            if (preferences) {
                log().info(context.currentKey(), "will send " + getClass().getSimpleName() + "Notification because build matches and user preferences allow it");
            } else {
                log().debug(context.currentKey(), "will NOT send " + getClass().getSimpleName() + "Notification - build matches but user preferences do not allow it");
            }
        } else {
            log().debug(context.currentKey(), "does not match " + getClass().getSimpleName() + "Notification condition");
        }
        return isMet && preferences;
    }
}
