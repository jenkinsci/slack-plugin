package jenkins.plugins.slack.webhook;


import jenkins.plugins.slack.webhook.model.SlackPostData;




public abstract class SlackRouterCommand {
    private SlackPostData data;

    public SlackRouterCommand(SlackPostData data) {
        this.data = data;
    }

    public SlackPostData getData() {
        return this.data;
    }

    public void setData(SlackPostData data) {
        this.data = data;
    }
}
