package jenkins.plugins.slack;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SlackNotifierUnitTest {
    private SlackNotifier slackNotifier = new SlackNotifier(CommitInfoChoice.AUTHORS);

    @Test
    public void isAnyCustomMessagePopulatedReturnsFalseWhenNonePopulated() {
        assertFalse(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    public void isAnyCustomMessagePopulatedReturnsTrueWhenOnePopulated() {
        slackNotifier.setCustomMessage("hi");

        assertTrue(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    public void isAnyCustomMessagePopulatedReturnsTrueWhenMultiplePopulated() {
        slackNotifier.setCustomMessage("hi");
        slackNotifier.setCustomMessageFailure("hii");

        assertTrue(slackNotifier.isAnyCustomMessagePopulated());
    }
}
