package jenkins.plugins.slack;

public class StandardSlackServiceBuilder {

    String baseUrl;
    String teamDomain;
    boolean botUser;
    String roomId;
    boolean replyBroadcast;
    boolean sendAsText;
    String iconEmoji;
    String username;
    String populatedToken;

    public StandardSlackServiceBuilder() {
    }

    public StandardSlackServiceBuilder withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public StandardSlackServiceBuilder withTeamDomain(String teamDomain) {
        this.teamDomain = teamDomain;
        return this;
    }

    public StandardSlackServiceBuilder withBotUser(boolean botUser) {
        this.botUser = botUser;
        return this;
    }

    public StandardSlackServiceBuilder withRoomId(String roomId) {
        this.roomId = roomId;
        return this;
    }

    public StandardSlackServiceBuilder withReplyBroadcast(boolean replyBroadcast) {
        this.replyBroadcast = replyBroadcast;
        return this;
    }

    public StandardSlackServiceBuilder withSendAsText(boolean sendAsText) {
        this.sendAsText = sendAsText;
        return this;
    }

    public StandardSlackServiceBuilder withIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
        return this;
    }

    public StandardSlackServiceBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public StandardSlackServiceBuilder withPopulatedToken(String populatedToken) {
        this.populatedToken = populatedToken;
        return this;
    }

    public StandardSlackService build() { return new StandardSlackService(this); }

}
