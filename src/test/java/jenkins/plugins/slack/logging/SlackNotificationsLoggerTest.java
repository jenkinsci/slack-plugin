package jenkins.plugins.slack.logging;

import hudson.model.TaskListener;
import java.io.PrintStream;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SlackNotificationsLoggerTest {
    @Mock
    private Logger system;
    @Mock
    private TaskListener user;
    @Captor
    private ArgumentCaptor<Supplier<String>> messageSupplier;
    private SlackNotificationsLogger logger;

    private AutoCloseable autoCloseable;

    @Before
    public void setup() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        logger = new SlackNotificationsLogger(system, user);
    }

    @After
    public void fin() throws Exception {
        autoCloseable.close();
    }

    @Test
    public void shouldOnlyWriteDebugMessagesToSystemLog() {
        String expected = "[key] this message has number 15 within it";

        logger.debug("[key]", "this message has number %d %s it", 15, "within");

        verifyNoMoreInteractions(user);
        verify(system).fine(messageSupplier.capture());
        assertEquals(expected, messageSupplier.getValue().get());
    }

    @Test
    public void shouldWriteInfoMessagesToSystemAndUserLogs() {
        String expectedUserLog = "[Slack Notifications] a 100% useful sort of message";
        String expectedSystemLog = "[Project #17] a 100% useful sort of message";

        PrintStream printStream = mock(PrintStream.class);
        when(user.getLogger()).thenReturn(printStream);

        logger.info("[Project #17]", "a %s useful sort %s message", "100%", "of");

        verify(user.getLogger()).println(expectedUserLog);
        verify(system).info(messageSupplier.capture());
        assertEquals(expectedSystemLog, messageSupplier.getValue().get());
    }
}
