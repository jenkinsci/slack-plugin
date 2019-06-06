package jenkins.plugins.slack;

import hudson.model.Item;
import jenkins.plugins.slack.matrix.MatrixTriggerMode;

public class SlackNotifierStub extends SlackNotifier {

    public SlackNotifierStub(String baseUrl, String teamDomain, String authToken, boolean botUser, String room, String authTokenCredentialId,
                             String sendAs, boolean startNotification, boolean notifyAborted, boolean notifyFailure,
                             boolean notifyNotBuilt, boolean notifySuccess, boolean notifyUnstable, boolean notifyRegression, boolean notifyBackToNormal,
                             boolean notifyRepeatedFailure, boolean includeTestSummary, boolean includeFailedTests, MatrixTriggerMode matrixTriggerMode,
                             CommitInfoChoice commitInfoChoice, boolean includeCustomMessage, String customMessage, String customMessageSuccess,
                             String customMessageAborted, String customMessageNotBuilt, String customMessageUnstable, String customMessageFailure) {
        super(new SlackNotifierBuilder()
                .withBaseUrl(baseUrl)
                .withTeamDomain(teamDomain)
                .withAuthToken(authToken)
                .withBotUser(botUser)
                .withRoom(room)
                .withTokenCredentialId(authTokenCredentialId)
                .withSendAs(sendAs)
                .withStartNotification(startNotification)
                .withNotifyAborted(notifyAborted)
                .withNotifyFailure(notifyFailure)
                .withNotifyNotBuilt(notifyNotBuilt)
                .withNotifySuccess(notifySuccess)
                .withNotifyUnstable(notifyUnstable)
                .withNotifyRegression(notifyRegression)
                .withNotifyBackToNormal(notifyBackToNormal)
                .withNotifyRepeatedFailure(notifyRepeatedFailure)
                .withIncludeTestSummary(includeTestSummary)
                .withIncludeFailedTests(includeFailedTests)
                .withMatrixTriggerMode(matrixTriggerMode)
                .withCommitInfoChoice(commitInfoChoice)
                .withIncludeCustomMessage(includeCustomMessage)
                .withCustomMessage(customMessage)
                .withCustomMessageSuccess(customMessageSuccess)
                .withCustomMessageAborted(customMessageAborted)
                .withCustomMessageNotBuilt(customMessageNotBuilt)
                .withCustomMessageUnstable(customMessageUnstable)
                .withCustomMessageFailure(customMessageFailure)
        );
    }


    public static class DescriptorImplStub extends SlackNotifier.DescriptorImpl {

        private SlackService slackService;

        @Override
        public synchronized void load() {
        }

        @Override
        SlackService getSlackService(final String baseUrl, final String teamDomain, final String authTokenCredentialId, final boolean botUser, final String room, final Item item) {
            return slackService;
        }

        public void setSlackService(SlackService slackService) {
            this.slackService = slackService;
        }
    }
}
