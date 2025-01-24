package jenkins.plugins.slack.decisions;

import hudson.model.Result;
import hudson.tasks.junit.TestResultAction;
import jenkins.plugins.slack.logging.BuildAwareLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OnRegressionTest {
    @Mock
    private Context context;
    @Mock
    private BuildAwareLogger log;
    @Mock
    private TestResultAction previousTestResult;
    @Mock
    private TestResultAction currentTestResult;

    private OnRegression condition = new OnRegression(null, log);

    @Test
    void shouldMeetConditionIfCurrentIsWorseThanPrevious() {
        given(context.currentResultOrSuccess()).willReturn(Result.FAILURE);
        given(context.previousResultOrSuccess()).willReturn(Result.SUCCESS);

        boolean actual = condition.isMetBy(context);

        assertTrue(actual);
    }

    @Test
    void shouldNotMeetConditionIfCurrentIsBetterThanPrevious() {
        given(context.currentResultOrSuccess()).willReturn(Result.SUCCESS);
        given(context.previousResultOrSuccess()).willReturn(Result.FAILURE);

        boolean actual = condition.isMetBy(context);

        assertFalse(actual);
    }

    @Test
    void shouldMeetConditionIfCurrentIsHasMoreTestFailuresThanPrevious() {
        given(context.currentResultOrSuccess()).willReturn(Result.UNSTABLE);
        given(context.previousResultOrSuccess()).willReturn(Result.UNSTABLE);
        given(context.getPreviousTestResult()).willReturn(previousTestResult);
        given(context.getCurrentTestResult()).willReturn(currentTestResult);
        given(previousTestResult.getFailCount()).willReturn(1);
        given(currentTestResult.getFailCount()).willReturn(10);

        boolean actual = condition.isMetBy(context);

        assertTrue(actual);
    }

    @Test
    void shouldNotMeetConditionIfCurrentIsHasFewerTestFailuresThanPrevious() {
        given(context.currentResultOrSuccess()).willReturn(Result.UNSTABLE);
        given(context.previousResultOrSuccess()).willReturn(Result.UNSTABLE);
        given(context.getPreviousTestResult()).willReturn(previousTestResult);
        given(context.getCurrentTestResult()).willReturn(currentTestResult);
        given(previousTestResult.getFailCount()).willReturn(10);
        given(currentTestResult.getFailCount()).willReturn(1);

        boolean actual = condition.isMetBy(context);

        assertFalse(actual);
    }
}
