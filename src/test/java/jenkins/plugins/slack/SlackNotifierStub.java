package jenkins.plugins.slack;

public class SlackNotifierStub extends SlackNotifier {

    public SlackNotifierStub(String teamDomain, String authToken, String room, String buildServerUrl,
                             String sendAs, boolean startNotification, boolean notifyAborted, boolean notifyFailure,
                             boolean notifyNotBuilt, boolean notifySuccess, boolean notifyUnstable, boolean notifyBackToNormal,
                             boolean notifyRepeatedFailure, boolean includeTestSummary, boolean showCommitList,
                             boolean includeCustomMessage, String customMessage, String jobPrefix) {
        super(teamDomain, authToken, room, buildServerUrl, sendAs, startNotification, notifyAborted, notifyFailure,
                notifyNotBuilt, notifySuccess, notifyUnstable, notifyBackToNormal, notifyRepeatedFailure,
                includeTestSummary, showCommitList, includeCustomMessage, customMessage, jobPrefix);
    }

    public static class DescriptorImplStub extends SlackNotifier.DescriptorImpl {

        private SlackService slackService;

        @Override
        public synchronized void load() {
        }

        @Override
        SlackService getSlackService(final String teamDomain, final String authToken, final String room) {
            return slackService;
        }

        public void setSlackService(SlackService slackService) {
            this.slackService = slackService;
        }
    }
}
