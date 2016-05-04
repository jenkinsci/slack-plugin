package jenkins.plugins.slack.webhook.model;




public class SlackTextMessage {
    private String text;
        
    public SlackTextMessage() { }

    public SlackTextMessage(String text) {
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }
}
