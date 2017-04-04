package jenkins.plugins.slack;

import java.util.List;

public class SlackNotifierStub extends SlackNotifier {

    public SlackNotifierStub(String baseUrl, String teamDomain, String authToken, boolean botUser, String room, String authTokenCredentialId, String apiToken,
                             String sendAs, boolean startNotification, boolean notifyAborted, boolean notifyFailure,
                             boolean notifyNotBuilt, boolean notifySuccess, boolean notifyUnstable, boolean notifyRegression, boolean notifyBackToNormal,
                             boolean notifyRepeatedFailure, boolean includeTestSummary, boolean includeFailedTests,
                             CommitInfoChoice commitInfoChoice, boolean includeCustomMessage, String customMessage, boolean includeMention, List<Mention> mentionList) {
        super(baseUrl, teamDomain, authToken, botUser, room, apiToken, authTokenCredentialId, sendAs, startNotification, notifyAborted, notifyFailure,
                notifyNotBuilt, notifySuccess, notifyUnstable, notifyRegression, notifyBackToNormal, notifyRepeatedFailure,
                includeTestSummary, includeFailedTests, commitInfoChoice, includeCustomMessage, customMessage, includeMention, mentionList);
    }

    public static class DescriptorImplStub extends SlackNotifier.DescriptorImpl {

        private SlackService slackService;

        @Override
        public synchronized void load() {
        }

        @Override
        SlackService getSlackService(final String baseUrl, final String teamDomain, final String authToken, final String authTokenCredentialId, final boolean botUser, final String room, final String apiToken) {
            return slackService;
        }

        public void setSlackService(SlackService slackService) {
            this.slackService = slackService;
        }
    }
}
