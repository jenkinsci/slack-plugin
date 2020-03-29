package jenkins.plugins.slack.decisions;

import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.TestResult;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jenkins.plugins.slack.SlackNotifier;
import jenkins.plugins.slack.logging.BuildAwareLogger;

public class OnRegression implements Condition {
    private final SlackNotifier preferences;
    private final BuildAwareLogger log;

    public OnRegression(SlackNotifier preferences, BuildAwareLogger log) {
        this.preferences = preferences;
        this.log = log;
    }

    @Override
    public boolean isMetBy(Context context) {
        return context.currentResultOrSuccess().isWorseThan(context.previousResultOrSuccess())
            || moreTestFailuresThanPrevious(context);
    }

    @Override
    public boolean userPreferenceMatches() {
        return preferences.getNotifyRegression();
    }

    @Override
    public BuildAwareLogger log() {
        return log;
    }

    private boolean moreTestFailuresThanPrevious(Context context) {
        TestResultAction currentTestResult = context.getCurrentTestResult();
        TestResultAction previousTestResult = context.getPreviousTestResult();
        if (currentTestResult != null && previousTestResult != null) {
            if (currentTestResult.getFailCount() > previousTestResult.getFailCount())
                return true;

            // test if different tests failed.
            return !getFailedTestIds(currentTestResult).equals(getFailedTestIds(previousTestResult));
        }
        return false;
    }

    private Set<String> getFailedTestIds(TestResultAction testResultAction) {
        Set<String> failedTestIds = new HashSet<>();
        List<? extends TestResult> failedTests = testResultAction.getFailedTests();
        for (TestResult result : failedTests) {
            failedTestIds.add(result.getId());
        }
        return failedTestIds;
    }

}
