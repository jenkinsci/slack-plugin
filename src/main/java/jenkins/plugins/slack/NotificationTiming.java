package jenkins.plugins.slack;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import hudson.model.Run;

public enum NotificationTiming {
    ABORTED("Build Aborted"),
    FAILURE("Build Failure"),
    NOT_BUILT("Build Not Built"),
    SUCCESS("Build Success"),
    UNSTABLE("Build Unstable"),
    BACK_TO_NORMAL("Build Back To Normal"),
    REPEATED_FAILURE("Build Repeated Failure"),
    REGRESSION("Build Regression");

    private final String displayName;

    private NotificationTiming(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static NotificationTiming forDisplayName(String displayName) {
        for (NotificationTiming notificationTiming : values()) {
            if (notificationTiming.getDisplayName().equals(displayName)) {
                return notificationTiming;
            }
        }
        return null;
    }

    public static NotificationTiming forBuildResult(AbstractBuild r, SlackNotifier notifier) {
        Result result = r.getResult();
        Result previousResult;
        Run previousBuild = r.getProject().getLastBuild().getPreviousBuild();
        Run previousSuccessfulBuild = r.getPreviousSuccessfulBuild();
        boolean buildHasSucceededBefore = previousSuccessfulBuild != null;

        Run lastNonAbortedBuild = previousBuild;
        while(lastNonAbortedBuild != null && lastNonAbortedBuild.getResult() == Result.ABORTED) {
            lastNonAbortedBuild = lastNonAbortedBuild.getPreviousBuild();
        }

        if(lastNonAbortedBuild == null) {
            previousResult = Result.SUCCESS;
        } else {
            previousResult = lastNonAbortedBuild.getResult();
        }

        if (result == Result.SUCCESS
                && (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE)
                && buildHasSucceededBefore && notifier.getNotifyBackToNormal()) {
            return BACK_TO_NORMAL;
        }
        if (result == Result.FAILURE && previousResult == Result.FAILURE) {
            return REPEATED_FAILURE;
        }
        if (result == Result.SUCCESS) {
            return SUCCESS;
        }
        if (result == Result.FAILURE) {
            return FAILURE;
        }
        if (result == Result.ABORTED) {
            return ABORTED;
        }
        if (result == Result.NOT_BUILT) {
            return NOT_BUILT;
        }
        if (result == Result.UNSTABLE) {
            return SUCCESS;
        }
        if (lastNonAbortedBuild != null && result.isWorseThan(previousResult)) {
            return REGRESSION;
        }
        return null;
    }
}
