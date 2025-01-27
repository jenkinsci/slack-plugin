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
class OnEveryFailureTest {
    @Mock
    private Context context;
    @Mock
    private BuildAwareLogger log;
    private OnEveryFailure condition = new OnEveryFailure(null, log);

    @Test
    void shouldMeetConditionIfCurrentIsFailure() {
        given(context.currentResult()).willReturn(Result.FAILURE);

        boolean actual = condition.isMetBy(context);

        assertTrue(actual);
    }

    @Test
    void shouldNotMeetConditionIfCurrentIsUnstable() {
        given(context.currentResult()).willReturn(Result.UNSTABLE);

        boolean actual = condition.isMetBy(context);

        assertFalse(actual);
    }
}
