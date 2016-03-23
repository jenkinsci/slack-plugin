package jenkins.plugins.slack.config;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.util.ReflectionUtils;

import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Job;

/**
 * Configuration migrator for migrating the Slack plugin configuration for an
 * {@link AbstractProject} that belongs to an {@link Item} as a template, from
 * the 1.8 format to the 2.0 format.
 * 
 * <p>
 * This is a workaround for installations that use the multi-branch-project
 * plugin, which uses a template to configure jobs for all branches in a repo.
 * The template will be updated by an {@link AbstractProjectConfigMigrator}.
 * </p>
 */
public class ItemWithTemplateConfigMigrator {

    private static final Logger logger = Logger.getLogger(ItemWithTemplateConfigMigrator.class
            .getName());

    private AbstractProjectConfigMigrator projectMigrator;

    /**
     * Default constructor.
     * 
     * @param projectMigrator
     *            Migrator to be used for migrating the template
     */
    public ItemWithTemplateConfigMigrator(final AbstractProjectConfigMigrator projectMigrator) {
        this.projectMigrator = projectMigrator;
    }

    /**
     * Migrate an item if it has a template that is a subclass of
     * AbstractProject.
     * 
     * @param item
     *            Item to migrate
     * @return true if migration was attempted on a template
     */
    public boolean migrate(final Item item) {
        AbstractProject<?, ?> project = getTemplateProject(item);

        if (project != null) {
            projectMigrator.migrate(project);
            return true;
        }

        return false;
    }

    /**
     * Examine an Item to determine if it has a "getTemplate" method that
     * returns an AbstractProject, and return the AbstractProject if it does.
     * 
     * @param item
     *            Item to examine
     * @return AbstractProject that is returned by item.getTemplate(), or null
     */
    private AbstractProject<?, ?> getTemplateProject(final Item item) {

        logger.log(Level.FINE,
                String.format("Checking \"%s\" for AbstractProject template", item.getName()));

        Method getTemplate = ReflectionUtils.findMethod(item.getClass(), "getTemplate");

        if (getTemplate == null) {
            logger.log(Level.FINE, "No template getter method found");
            return null;
        }

        Object obj = null;

        try {
            obj = getTemplate.invoke(item);
        } catch (Exception e) {
            logger.info("Error getting \"template\" value: " + e.getMessage());
            return null;
        }

        if (obj == null) {
            logger.log(Level.FINE, "Item has no template");
        } else if (obj instanceof AbstractProject) {
            return (AbstractProject<?, ?>) obj;
        } else {
            logger.log(Level.FINE, "Template is not an AbstractProject; type is: "
                    + obj.getClass().getName());
        }

        return null;
    }
}
