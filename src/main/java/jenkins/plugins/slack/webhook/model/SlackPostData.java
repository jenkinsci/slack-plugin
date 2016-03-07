package jenkins.plugins.slack.webhook.model;




public class SlackPostData {
    private String text;
    private String token;
    private String team_id;
    private String team_domain;
    private String channel_id;
    private String channel_name;
    private String timestamp;
    private String user_id;
    private String user_name;
    private String trigger_word;

    public SlackPostData() {

    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }

    public void setTeam_id(String team_id) {
        this.team_id = team_id;
    }

    public String getTeam_id() {
        return this.team_id;
    }

    public void setTeam_domain(String team_domain) {
        this.team_domain = team_domain;
    }

    public String getTeam_domain() {
        return this.team_domain;
    }

    public void setChannel_id(String channel_id) {
        this.channel_id = channel_id;
    }

    public String getChannel_id() {
        return this.channel_id;
    }

    public void setChannel_name(String channel_name) {
        this.channel_name = channel_name;
    }

    public String getChannel_name() {
        return this.channel_name;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_id() {
        return this.user_id;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_name() {
        return this.user_name;
    }

    public void setTrigger_word(String trigger_word) {
        this.trigger_word = trigger_word;
    }

    public String getTrigger_word() {
        return this.trigger_word;
    }
}

