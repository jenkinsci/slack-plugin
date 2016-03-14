package jenkins.plugins.slack.webhook.model;

import hudson.model.Cause;




public class SlackWebhookCause extends Cause {
    private String username;

    public SlackWebhookCause(String username) {
        this.username = username;
    }

    @Override
    public String getShortDescription() {
        return "Build started by Slack user @"+username+ " via SlackWebhookPlugin";
    }
}
