package jenkins.plugins.slack.decisions;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationConditionsTest {

    @Test
    void shouldMatchIfAnyConditionMatches() {
        NotificationConditions conditions = new NotificationConditions(Arrays.asList(
                p -> false,
                p -> false,
                p -> true
        ));

        boolean actual = conditions.test(null);

        assertTrue(actual);
    }
}
