package jenkins.plugins.slack;

public enum NotificationTiming {
    ABORTED("Build Aborted", ActiveNotifier.MessageBuilder.ABORTED_STATUS_MESSAGE),
    FAILURE("Build Failure", ActiveNotifier.MessageBuilder.FAILURE_STATUS_MESSAGE),
    NOT_BUILT("Build Not Built", ActiveNotifier.MessageBuilder.NOT_BUILT_STATUS_MESSAGE),
    SUCCESS("Build Success", ActiveNotifier.MessageBuilder.SUCCESS_STATUS_MESSAGE),
    UNSTABLE("Build Unstable", ActiveNotifier.MessageBuilder.UNSTABLE_STATUS_MESSAGE),
    BACK_TO_NORMAL("Build Back To Normal", ActiveNotifier.MessageBuilder.BACK_TO_NORMAL_STATUS_MESSAGE),
    REPEATED_FAILURE("Build Repeated Failure", ActiveNotifier.MessageBuilder.STILL_FAILING_STATUS_MESSAGE);

    private final String displayName;
    private final String statusMessage;

    private NotificationTiming(String displayName, String statusMessage) {
        this.displayName = displayName;
        this.statusMessage = statusMessage;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    public static NotificationTiming forDisplayName(String displayName) {
        for (NotificationTiming notificationTiming : values()) {
            if (notificationTiming.getDisplayName().equals(displayName)) {
                return notificationTiming;
            }
        }
        return null;
    }

    public static NotificationTiming forStatusMessage(String statusMessage) {
        for (NotificationTiming notificationTiming : values()) {
            if (notificationTiming.getStatusMessage().equals(statusMessage)) {
                return notificationTiming;
            }
        }
        return null;
    }
}
