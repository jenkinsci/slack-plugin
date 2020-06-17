package jenkins.plugins.slack.logging;

import java.io.PrintStream;

public interface BuildAwareLogger {
    void debug(String key, String message, Object... args);
    void info(String key, String message, Object... args);
    PrintStream getUserLogger();
}
