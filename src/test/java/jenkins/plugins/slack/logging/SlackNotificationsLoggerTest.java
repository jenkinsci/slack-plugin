package jenkins.plugins.slack.logging;

import java.io.PrintStream;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class SlackNotificationsLoggerTest {
    @Mock
    private Logger system;
    @Mock
    private PrintStream user;
    @Captor
    private ArgumentCaptor<Supplier<String>> messageSupplier;
    private SlackNotificationsLogger logger;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        logger = new SlackNotificationsLogger(system, user);
    }

    @Test
    public void shouldOnlyWriteDebugMessagesToSystemLog() {
        String expected = "[key] this message has number 15 within it";

        logger.debug("[key]", "this message has number %d %s it", 15, "within");

        verifyZeroInteractions(user);
        verify(system).fine(messageSupplier.capture());
        assertEquals(expected, messageSupplier.getValue().get());
    }

    @Test
    public void shouldWriteInfoMessagesToSystemAndUserLogs() {
        String expectedUserLog = "[Slack Notifications] a 100% useful sort of message";
        String expectedSystemLog = "[Project #17] a 100% useful sort of message";

        logger.info("[Project #17]", "a %s useful sort %s message", "100%", "of");

        verify(user).println(expectedUserLog);
        verify(system).info(messageSupplier.capture());
        assertEquals(expectedSystemLog, messageSupplier.getValue().get());
    }
}
