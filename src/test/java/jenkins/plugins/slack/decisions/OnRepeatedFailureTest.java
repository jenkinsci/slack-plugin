package jenkins.plugins.slack.decisions;

import hudson.model.Result;
import jenkins.plugins.slack.logging.BuildAwareLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OnRepeatedFailureTest {
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Context context;
    @Mock
    private BuildAwareLogger log;
    private OnRepeatedFailure condition = new OnRepeatedFailure(null, log);

    @Test
    void shouldMeetConditionIfCurrentAndPreviousAreFailures() {
        given(context.previousResultOrSuccess()).willReturn(Result.FAILURE);
        given(context.currentResult()).willReturn(Result.FAILURE);

        boolean actual = condition.isMetBy(context);

        assertTrue(actual);
    }

    @Test
    void shouldNotMeetConditionIfCurrentIsUnstable() {
        given(context.previousResultOrSuccess()).willReturn(Result.FAILURE);
        given(context.currentResult()).willReturn(Result.UNSTABLE);

        boolean actual = condition.isMetBy(context);

        assertFalse(actual);
    }

    @Test
    void shouldNotMeetConditionIfPreviousIsUnstable() {
        given(context.previousResultOrSuccess()).willReturn(Result.UNSTABLE);
        given(context.currentResult()).willReturn(Result.FAILURE);

        boolean actual = condition.isMetBy(context);

        assertFalse(actual);
    }
}
