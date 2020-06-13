package jenkins.plugins.slack.logging;

public interface BuildAwareLogger {
    void debug(String key, String message, Object... args);
    void info(String key, String message, Object... args);
}
