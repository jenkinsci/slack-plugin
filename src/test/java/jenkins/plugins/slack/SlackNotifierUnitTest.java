package jenkins.plugins.slack;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SlackNotifierUnitTest {

    @Test
    public void isAnyCustomMessagePopulatedReturnsFalseWhenNonePopulated() {
        SlackNotifier slackNotifier = new SlackNotifier(CommitInfoChoice.AUTHORS);

        assertFalse(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    public void isAnyCustomMessagePopulatedReturnsTrueWhenOnePopulated() {
        SlackNotifier slackNotifier = new SlackNotifier(CommitInfoChoice.AUTHORS);
        slackNotifier.setCustomMessage("hi");

        assertTrue(slackNotifier.isAnyCustomMessagePopulated());
    }

    @Test
    public void isAnyCustomMessagePopulatedReturnsTrueWhenMultiplePopulated() {
        SlackNotifier slackNotifier = new SlackNotifier(CommitInfoChoice.AUTHORS);
        slackNotifier.setCustomMessage("hi");
        slackNotifier.setCustomMessageFailure("hii");

        assertTrue(slackNotifier.isAnyCustomMessagePopulated());
    }
}
