package jenkins.plugins.slack;

import hudson.model.Item;
import jenkins.plugins.slack.matrix.MatrixTriggerMode;

public class SlackNotifierStub extends SlackNotifier {

    public SlackNotifierStub(String baseUrl, String teamDomain, String authToken, boolean botUser, String room, String authTokenCredentialId,
                             String sendAs, String iconEmoji, String username, boolean startNotification, boolean notifyAborted, boolean notifyFailure,
                             boolean notifyNotBuilt, boolean notifySuccess, boolean notifyUnstable, boolean notifyRegression, boolean notifyBackToNormal,
                             boolean notifyRepeatedFailure, boolean includeTestSummary, boolean includeFailedTests, MatrixTriggerMode matrixTriggerMode,
                             CommitInfoChoice commitInfoChoice, boolean includeCustomMessage, String customMessage, String customMessageSuccess,
                             String customMessageAborted, String customMessageNotBuilt, String customMessageUnstable, String customMessageFailure) {
        super(baseUrl, teamDomain, authToken, botUser, room, authTokenCredentialId, sendAs, iconEmoji, username, startNotification, notifyAborted, notifyFailure,
                notifyNotBuilt, notifySuccess, notifyUnstable, notifyRegression, notifyBackToNormal, notifyRepeatedFailure,
                includeTestSummary, includeFailedTests, matrixTriggerMode, commitInfoChoice, includeCustomMessage, customMessage,
                customMessageSuccess, customMessageAborted, customMessageNotBuilt, customMessageUnstable, customMessageFailure);
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
