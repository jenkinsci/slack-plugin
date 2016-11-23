package jenkins.plugins.slack.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import hudson.model.ItemGroup;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import jenkins.model.AbstractTopLevelItem;
import jenkins.model.Jenkins;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

@SuppressWarnings("deprecation")
public class ItemWithTemplateConfigMigratorTest {

    private Jenkins jenkins;
    private ItemWithTemplateConfigMigrator migrator;
    private AbstractProjectConfigMigrator projectMigratorMock;

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Before
    public void setUp() {
        jenkins = j.getInstance();
        projectMigratorMock = mock(AbstractProjectConfigMigrator.class);
        migrator = new ItemWithTemplateConfigMigrator(projectMigratorMock);
    }

    @Test
    public void testMigrate_WithNoTemplateMethod() {
        FreeStyleProject project = new FreeStyleProject(jenkins, "Test_Slack_Plugin");
        assertFalse("Should not be able to find template project", migrator.migrate(project));
        verify(projectMigratorMock, never()).migrate(any(AbstractProject.class));
    }

    @Test
    public void testMigrate_WithNoTemplate() {
        ProjectWithTemplate project = new ProjectWithTemplate(jenkins, "Test", null);
        assertFalse("Should be no template project", migrator.migrate(project));
        verify(projectMigratorMock, never()).migrate(any(AbstractProject.class));
    }

    @Test
    public void testMigrate_WithTemplateWrongType() {
        ProjectWithTemplate project = new ProjectWithTemplate(jenkins, "Test", "NOT A PROJECT");
        assertFalse("Template is not correct type", migrator.migrate(project));
        verify(projectMigratorMock, never()).migrate(any(AbstractProject.class));
    }

    @Test
    public void testMigrate_WithTemplate() {
        FreeStyleProject template = new FreeStyleProject(jenkins, "Test_Slack_Plugin");
        ProjectWithTemplate project = new ProjectWithTemplate(jenkins, "Test", template);
        assertTrue("Template was migrated", migrator.migrate(project));
        verify(projectMigratorMock, times(1)).migrate(eq(template));
    }

    @Test
    public void testMigrate_WithException() {
        FreeStyleProject template = new FreeStyleProject(jenkins, "Test_Slack_Plugin");
        ProjectWithTemplate project = new ProjectWithTemplate(jenkins, "Test", template, true);
        assertFalse(migrator.migrate(project));
        verify(projectMigratorMock, never()).migrate(any(AbstractProject.class));
    }

    private static class ProjectWithTemplate extends AbstractTopLevelItem {
        private Object template;
        private boolean doThrow = false;

        public ProjectWithTemplate(ItemGroup<?> group, String name, Object template) {
            super(group, name);
            this.template = template;
        }

        public ProjectWithTemplate(ItemGroup<?> group, String name, Object template, boolean doThrow) {
            super(group, name);
            this.template = template;
            this.doThrow = doThrow;
        }

        @SuppressWarnings("unused")
        public Object getTemplate() {
            if (doThrow) {
                throw new RuntimeException("Something bad happened");
            }
            return template;
        }
    }
}
