package jenkins.plugins.slack.decisions;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ContextTest {
    @Mock
    private AbstractBuild<?, ?> previous;
    @Mock
    private AbstractBuild<?, ?> current;

    @Test
    void shouldReturnSuccessIfPreviousBuildNull() {
        Context context = new Context(current, null);
        Result expected = Result.SUCCESS;

        Result actual = context.previousResultOrSuccess();

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnSuccessIfPreviousResultNull() {
        given(previous.getResult()).willReturn(null);
        Context context = new Context(current, previous);
        Result expected = Result.SUCCESS;

        Result actual = context.previousResultOrSuccess();

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnPreviousResultIfPresent() {
        Result expected = Result.UNSTABLE;
        given(previous.getResult()).willReturn(expected);
        Context context = new Context(current, previous);

        Result actual = context.previousResultOrSuccess();

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnCurrentResultIfPresent() {
        Result expected = Result.NOT_BUILT;
        given(current.getResult()).willReturn(expected);
        Context context = new Context(current, previous);

        Result actual = context.currentResult();

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnNullIfCurrentBuildNull() {
        Context context = new Context(null, previous);

        Result actual = context.currentResult();

        assertNull(actual);
    }

    @Test
    void shouldReturnNullIfCurrentResultNull() {
        given(current.getResult()).willReturn(null);
        Context context = new Context(current, previous);

        Result actual = context.currentResult();

        assertNull(actual);
    }
}
