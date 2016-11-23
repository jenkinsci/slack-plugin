package jenkins.plugins.slack.config;

import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Job;
import hudson.util.DescribableList;

/**
 * Configuration migrator for migrating the Slack plugin configuration from the
 * 1.8 format to the 2.0 format, for an item like a job or project. Mainly just
 * decides what needs to be done and delegates the actual migration work to
 * other migrators.
 * 
 * @see AbstractProjectConfigMigrator
 * @see JobConfigMigrator
 * @see ItemWithTemplateConfigMigrator
 */
public class ItemConfigMigrator {

    private final AbstractProjectConfigMigrator projectMigrator;
    private final JobConfigMigrator jobMigrator;
    private final ItemWithTemplateConfigMigrator templateMigrator;

    public ItemConfigMigrator() {
        projectMigrator = new AbstractProjectConfigMigrator();
        jobMigrator = new JobConfigMigrator();
        templateMigrator = new ItemWithTemplateConfigMigrator(projectMigrator);
    }
    
    /**
     * Constructor for injecting migrators for testing.
     */
    protected ItemConfigMigrator(AbstractProjectConfigMigrator projectMigrator,
            JobConfigMigrator jobMigrator, ItemWithTemplateConfigMigrator templateMigrator) {
        this.projectMigrator = projectMigrator;
        this.jobMigrator = jobMigrator;
        this.templateMigrator = templateMigrator;
    }

    /**
     * Migrate configuration for a {@link Item} from the 1.8 format to the 2.0
     * format. This primarily removes job properties and adds them to a
     * notifier.
     * 
     * @param item
     *            Item to migrate
     * @return true if migration was attempted for an expected scenario; false
     *         if migration was not attempted due to an unexpected data type or
     *         other reason
     */
    public boolean migrate(Item item) {

        // Attempt migrations in priority order
        return migrateAbstractProject(item) || migrateJobWithoutPublishersList(item)
                || templateMigrator.migrate(item);
    }

    /**
     * Migrate an item if it is a subclass of AbstractProject.
     * 
     * @param item
     *            Item to migrate
     * @return true if migration was attempted
     */
    private boolean migrateAbstractProject(Item item) {

        if (item instanceof AbstractProject) {
            AbstractProject<?, ?> project = (AbstractProject<?, ?>) item;
            projectMigrator.migrate(project);
            return true;
        }
        return false;
    }

    /**
     * Migrate an item if it is subclass of Job. It might have Slack job
     * properties that need to be removed in the migration, but does not handle
     * the possibility of a Job that has publishers. That should theoretically
     * not happen because then it should be a subclass of AbstractProject, but
     * we want to avoid losing settings. So if it looks like it has publishers,
     * migration is skipped.
     * 
     * @param item
     *            Item to migrate
     * @return true if migration was attempted
     */
    private boolean migrateJobWithoutPublishersList(Item item) {
        if (item instanceof Job) {
            if (!hasMethodGetPublishersList(item)) {
                Job<?, ?> job = (Job<?, ?>) item;
                jobMigrator.migrate(job);
                return true;
            }
        }
        return false;
    }

    private boolean hasMethodGetPublishersList(Item item) {

        Method method = ReflectionUtils.findMethod(item.getClass(), "getPublishersList");

        if (method != null && method.getReturnType().equals(DescribableList.class)) {
            return true;
        }

        return false;
    }
}
