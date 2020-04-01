package jenkins.plugins.slack;

import hudson.model.Run;
import jenkins.plugins.slack.user.SlackUserIdResolver;

public class StandardSlackServiceBuilder {

    Run run;
    String baseUrl;
    String teamDomain;
    boolean botUser;
    String roomId;
    boolean replyBroadcast;
    String iconEmoji;
    String username;
    String populatedToken;
    boolean notifyCommitters;
    SlackUserIdResolver userIdResolver;

    public StandardSlackServiceBuilder() {
    }

    public StandardSlackServiceBuilder withRun(Run run) {
        this.run = run;
        return this;
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

    public StandardSlackServiceBuilder withNotifyCommitters(boolean notifyCommitters) {
        this.notifyCommitters = notifyCommitters;
        return this;
    }

    public StandardSlackServiceBuilder withSlackUserIdResolver(SlackUserIdResolver userIdResolver) {
        this.userIdResolver = userIdResolver;
        return this;
    }

    public StandardSlackService build() { return new StandardSlackService(this); }

}
