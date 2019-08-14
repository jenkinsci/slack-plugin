package jenkins.plugins.slack;

import jenkins.plugins.slack.matrix.MatrixTriggerMode;
import jenkins.plugins.slack.user.SlackUserIdResolver;

public class SlackNotifierBuilder {
    String baseUrl;
    String teamDomain;
    String authToken;
    String tokenCredentialId;
    boolean botUser;
    String room;
    String sendAs;
    String iconEmoji;
    String username;
    boolean startNotification;
    boolean notifySuccess;
    boolean notifyAborted;
    boolean notifyNotBuilt;
    boolean notifyUnstable;
    boolean notifyRegression;
    boolean notifyFailure;
    boolean notifyEveryFailure;
    boolean notifyBackToNormal;
    boolean notifyRepeatedFailure;
    boolean includeTestSummary;
    boolean includeFailedTests;
    MatrixTriggerMode matrixTriggerMode;
    CommitInfoChoice commitInfoChoice;
    boolean includeCustomMessage;
    String customMessage;
    String customMessageSuccess;
    String customMessageAborted;
    String customMessageNotBuilt;
    String customMessageUnstable;
    String customMessageFailure;
    SlackUserIdResolver slackUserIdResolver;

    SlackNotifierBuilder() {
    }

    public SlackNotifierBuilder withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public SlackNotifierBuilder withTeamDomain(String teamDomain) {
        this.teamDomain = teamDomain;
        return this;
    }

    public SlackNotifierBuilder withAuthToken(String authToken) {
        this.authToken = authToken;
        return this;
    }

    public SlackNotifierBuilder withBotUser(boolean botUser) {
        this.botUser = botUser;
        return this;
    }

    public SlackNotifierBuilder withRoom(String room) {
        this.room = room;
        return this;
    }

    public SlackNotifierBuilder withSendAs(String sendAs) {
        this.sendAs = sendAs;
        return this;
    }

    public SlackNotifierBuilder withIconEmoji(String iconEmoji) {
        this.iconEmoji = iconEmoji;
        return this;
    }

    public SlackNotifierBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public SlackNotifierBuilder withTokenCredentialId(String tokenCredentialId) {
        this.tokenCredentialId = tokenCredentialId;
        return this;
    }

    public SlackNotifierBuilder withStartNotification(boolean startNotification) {
        this.startNotification = startNotification;
        return this;
    }

    public SlackNotifierBuilder withNotifyAborted(boolean notifyAborted) {
        this.notifyAborted = notifyAborted;
        return this;
    }

    public SlackNotifierBuilder withNotifyNotBuilt(boolean notifyNotBuilt) {
        this.notifyNotBuilt = notifyNotBuilt;
        return this;
    }

    public SlackNotifierBuilder withNotifySuccess(boolean notifySuccess) {
        this.notifySuccess = notifySuccess;
        return this;
    }

    public SlackNotifierBuilder withNotifyUnstable(boolean notifyUnstable) {
        this.notifyUnstable = notifyUnstable;
        return this;
    }

    public SlackNotifierBuilder withNotifyRegression(boolean notifyRegression) {
        this.notifyRegression = notifyRegression;
        return this;
    }

    public SlackNotifierBuilder withNotifyFailure(boolean notifyFailure) {
        this.notifyFailure = notifyFailure;
        return this;
    }

    public SlackNotifierBuilder withNotifyEveryFailure(boolean notifyEveryFailure) {
        this.notifyEveryFailure = notifyEveryFailure;
        return this;
    }

    public SlackNotifierBuilder withNotifyBackToNormal(boolean notifyBackToNormal) {
        this.notifyBackToNormal = notifyBackToNormal;
        return this;
    }

    public SlackNotifierBuilder withNotifyRepeatedFailure(boolean notifyRepeatedFailure) {
        this.notifyRepeatedFailure = notifyRepeatedFailure;
        return this;
    }

    public SlackNotifierBuilder withIncludeTestSummary(boolean includeTestSummary) {
        this.includeTestSummary = includeTestSummary;
        return this;
    }

    public SlackNotifierBuilder withIncludeFailedTests(boolean includeFailedTests) {
        this.includeFailedTests = includeFailedTests;
        return this;
    }

    public SlackNotifierBuilder withMatrixTriggerMode(MatrixTriggerMode matrixTriggerMode) {
        this.matrixTriggerMode = matrixTriggerMode;
        return this;
    }

    public SlackNotifierBuilder withCommitInfoChoice(CommitInfoChoice commitInfoChoice) {
        this.commitInfoChoice = commitInfoChoice;
        return this;
    }

    public SlackNotifierBuilder withIncludeCustomMessage(boolean includeCustomMessage) {
        this.includeCustomMessage = includeCustomMessage;
        return this;
    }

    public SlackNotifierBuilder withCustomMessage(String customMessage) {
        this.customMessage = customMessage;
        return this;
    }

    public SlackNotifierBuilder withCustomMessageSuccess(String customMessageSuccess) {
        this.customMessageSuccess = customMessageSuccess;
        return this;
    }

    public SlackNotifierBuilder withCustomMessageAborted(String customMessageAborted) {
        this.customMessageAborted = customMessageAborted;
        return this;
    }

    public SlackNotifierBuilder withCustomMessageNotBuilt(String customMessageNotBuilt) {
        this.customMessageNotBuilt = customMessageNotBuilt;
        return this;
    }

    public SlackNotifierBuilder withCustomMessageUnstable(String customMessageUnstable) {
        this.customMessageUnstable = customMessageUnstable;
        return this;
    }

    public SlackNotifierBuilder withCustomMessageFailure(String customMessageFailure) {
        this.customMessageFailure = customMessageFailure;
        return this;
    }

    public SlackNotifierBuilder withSlackUserIdResolver(SlackUserIdResolver slackUserIdResolver) {
        this.slackUserIdResolver = slackUserIdResolver;
        return this;
    }

    public SlackNotifier build() {
        return new SlackNotifier(this);
    }
}
