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
class OnBackToNormalTest {
    @Mock
    private Context context;
    @Mock
    private BuildAwareLogger log;
    private OnBackToNormal subject = new OnBackToNormal(null, log);

    @Test
    void shouldMeetConditionIfPreviousFailureIsNowSuccess() {
        given(context.previousResultOrSuccess()).willReturn(Result.FAILURE);
        given(context.currentResult()).willReturn(Result.SUCCESS);

        boolean actual = subject.isMetBy(context);

        assertTrue(actual);
    }

    @Test
    void shouldMeetConditionIfPreviousUnstableIsNowSuccess() {
        given(context.previousResultOrSuccess()).willReturn(Result.UNSTABLE);
        given(context.currentResult()).willReturn(Result.SUCCESS);

        boolean actual = subject.isMetBy(context);

        assertTrue(actual);
    }

    @Test
    void shouldNotMeetConditionIfCurrentIsNotSuccess() {
        given(context.previousResultOrSuccess()).willReturn(Result.FAILURE);
        given(context.currentResult()).willReturn(Result.FAILURE);

        boolean actual = subject.isMetBy(context);

        assertFalse(actual);
    }

    @Test
    void shouldNotMeetConditionIfPreviousIsSuccess() {
        given(context.previousResultOrSuccess()).willReturn(Result.FAILURE);
        given(context.currentResult()).willReturn(Result.FAILURE);

        boolean actual = subject.isMetBy(context);

        assertFalse(actual);
    }

}
