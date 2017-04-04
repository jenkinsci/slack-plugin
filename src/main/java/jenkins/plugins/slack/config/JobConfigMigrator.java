package jenkins.plugins.slack.config;

import hudson.model.Job;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.plugins.slack.SlackNotifier.SlackJobProperty;

/**
 * Configuration migrator for migrating the Slack plugin configuration for a
 * {@link Job} from the 1.8 format to the 2.0 format. It does so by removing the
 * SlackJobProperty from the job properties (if there is one).
 * 
 * <p>
 * SlackJobProperty settings are usually migrated to a publisher, but there are
 * no publishers in a Job so the settings are lost. For this reason, <strong>be
 * careful of how you use this migrator.</strong>.
 * </p>
 */
@SuppressWarnings("deprecation")
public class JobConfigMigrator {

    private static final Logger logger = Logger.getLogger(JobConfigMigrator.class.getName());

    public void migrate(final Job<?, ?> job) {

        logger.info(String.format("Migrating job \"%s\" with type %s", job.getName(), job
                .getClass().getName()));

        final SlackJobProperty slackJobProperty = job.getProperty(SlackJobProperty.class);

        if (slackJobProperty == null) {
            logger.info(String.format(
                    "Configuration is already up to date for \"%s\", skipping migration",
                    job.getName()));
            return;
        }

        try {
            // property section is not used anymore - remove
            job.removeProperty(SlackJobProperty.class);
            job.save();
            logger.info(String.format("Configuration for \"%s\" updated successfully",
                    job.getName()));
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
