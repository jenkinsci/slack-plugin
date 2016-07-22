package jenkins.plugins.slack;

import java.util.List;

public class SlackNotifierStub extends SlackNotifier {

    public SlackNotifierStub(String teamDomain, String authToken, String room, String apiToken, String buildServerUrl,
                             String sendAs, boolean startNotification, boolean notifyAborted, boolean notifyFailure,
                             boolean notifyNotBuilt, boolean notifySuccess, boolean notifyUnstable, boolean notifyBackToNormal,
                             boolean notifyRepeatedFailure, boolean includeTestSummary, CommitInfoChoice commitInfoChoice,
                             boolean includeCustomMessage, String customMessage, boolean includeMention, List<Mention> mentionList) {
        super(teamDomain, authToken, room, apiToken, buildServerUrl, sendAs, startNotification, notifyAborted, notifyFailure,
                notifyNotBuilt, notifySuccess, notifyUnstable, notifyBackToNormal, notifyRepeatedFailure,
                includeTestSummary, commitInfoChoice, includeCustomMessage, customMessage, includeMention, mentionList);
    }

    public static class DescriptorImplStub extends SlackNotifier.DescriptorImpl {

        private SlackService slackService;

        @Override
        public synchronized void load() {
        }

        @Override
        SlackService getSlackService(final String teamDomain, final String authToken, final String room, final String apiToken) {
            return slackService;
        }

        public void setSlackService(SlackService slackService) {
            this.slackService = slackService;
        }
    }
}
