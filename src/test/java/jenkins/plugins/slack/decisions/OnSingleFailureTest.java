package jenkins.plugins.slack.decisions;

import hudson.model.Result;
import jenkins.plugins.slack.logging.BuildAwareLogger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

public class OnSingleFailureTest {
    @Mock
    private Context context;
    @Mock
    private BuildAwareLogger log;
    private OnSingleFailure condition = new OnSingleFailure(null, log);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldMeetConditionIfCurrentIsFailureAndPreviousIsUnstable() {
        given(context.previousResultOrSuccess()).willReturn(Result.UNSTABLE);
        given(context.currentResult()).willReturn(Result.FAILURE);

        boolean actual = condition.isMetBy(context);

        assertTrue(actual);
    }

    @Test
    public void shouldNotMeetConditionIfCurrentIsFailureAndPreviousIsFailure() {
        given(context.previousResultOrSuccess()).willReturn(Result.FAILURE);
        given(context.currentResult()).willReturn(Result.FAILURE);

        boolean actual = condition.isMetBy(context);

        assertFalse(actual);
    }

    @Test
    public void shouldNotMeetConditionIfCurrentIsSuccess() {
        given(context.previousResultOrSuccess()).willReturn(Result.FAILURE);
        given(context.currentResult()).willReturn(Result.SUCCESS);

        boolean actual = condition.isMetBy(context);

        assertFalse(actual);
    }

}