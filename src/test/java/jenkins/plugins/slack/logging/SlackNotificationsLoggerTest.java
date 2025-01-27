package jenkins.plugins.slack.logging;

import hudson.model.TaskListener;
import java.io.PrintStream;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlackNotificationsLoggerTest {
    @Mock
    private Logger system;
    @Mock
    private TaskListener user;
    @Captor
    private ArgumentCaptor<Supplier<String>> messageSupplier;
    private SlackNotificationsLogger logger;

    @BeforeEach
    void setup() {
        logger = new SlackNotificationsLogger(system, user);
    }

    @Test
    void shouldOnlyWriteDebugMessagesToSystemLog() {
        String expected = "[key] this message has number 15 within it";

        logger.debug("[key]", "this message has number %d %s it", 15, "within");

        verifyNoMoreInteractions(user);
        verify(system).fine(messageSupplier.capture());
        assertEquals(expected, messageSupplier.getValue().get());
    }

    @Test
    void shouldWriteInfoMessagesToSystemAndUserLogs() {
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
