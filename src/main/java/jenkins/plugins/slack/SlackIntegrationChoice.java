package jenkins.plugins.slack;

public enum SlackIntegrationChoice {
    APP("Jenkins CI app"),
    INCOMING_WEBHOOK("Incoming Webhook");

    private final String displayName;

    SlackIntegrationChoice(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }

    public static SlackIntegrationChoice forDisplayName(String displayName) {
        for (SlackIntegrationChoice slackIntegrationChoice : values()) {
            if (slackIntegrationChoice.toString().equals(displayName)) {
                return slackIntegrationChoice;
            }
        }
        return null;
    }
}
