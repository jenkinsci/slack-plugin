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

public class OnEveryFailureTest {
    @Mock
    private Context context;
    @Mock
    private BuildAwareLogger log;
    private OnEveryFailure condition = new OnEveryFailure(null, log);

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldMeetConditionIfCurrentIsFailure() {
        given(context.currentResult()).willReturn(Result.FAILURE);

        boolean actual = condition.isMetBy(context);

        assertTrue(actual);
    }

    @Test
    public void shouldNotMeetConditionIfCurrentIsUnstable() {
        given(context.currentResult()).willReturn(Result.UNSTABLE);

        boolean actual = condition.isMetBy(context);

        assertFalse(actual);
    }
}
