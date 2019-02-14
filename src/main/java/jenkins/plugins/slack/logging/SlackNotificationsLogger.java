package jenkins.plugins.slack.logging;

import hudson.model.AbstractBuild;
import jenkins.plugins.slack.SlackNotifier;

import java.io.PrintStream;
import java.util.logging.Logger;

public class SlackNotificationsLogger implements BuildAwareLogger {
    private static final String PLUGIN_KEY = String.format("[%s]", SlackNotifier.DescriptorImpl.PLUGIN_DISPLAY_NAME);
    private final Logger system;
    private final PrintStream user;

    public SlackNotificationsLogger(Logger system, PrintStream user) {
        this.system = system;
        this.user = user;
    }

    /**
     * Debug logs are only written to the system log.
     *
     * @see String#format(String, Object...) for message formatting options
     * @see BuildKey#format(AbstractBuild) to create a build key easily
     *
     * @param key - Human-readable representation of the build
     * @param message - message to be written to the system log
     * @param args - arguments for the message
     */
    @Override
    public void debug(String key, String message, Object... args) {
        system.fine(() -> String.join(" ", key, String.format(message, args)));
    }

    /**
     * Info logs are written to the system log with the build key and to the build's log without the key
     *
     * @see String#format(String, Object...) for message formatting options
     * @see BuildKey#format(AbstractBuild) to create a build key easily
     *
     * @param key - Human-readable representation of the build
     * @param message - message to be written to system log and build's console output
     * @param args - arguments for the message
     */
    @Override
    public void info(String key, String message, Object... args) {
        String formattedMessage = String.format(message, args);
        system.info(() -> String.join(" ", key, formattedMessage));
        user.println(String.join(" ", PLUGIN_KEY, formattedMessage));
    }
}
