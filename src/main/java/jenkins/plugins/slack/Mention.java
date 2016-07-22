package jenkins.plugins.slack;

public class Mention {
    private NotificationTiming timing;
    private String to;

    public Mention(NotificationTiming timing, String to) {
        this.timing = timing;
        this.to = to;
    }

    public NotificationTiming getTiming() {
        return timing;
    }

    public String getTo() {
        return to;
    }
}
