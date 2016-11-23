package jenkins.plugins.slack.config;

import jenkins.model.AbstractTopLevelItem;
import jenkins.model.Jenkins;
import hudson.model.Descriptor;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.ViewJob;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class ItemConfigMigratorTest {
    
    private AbstractProjectConfigMigrator projectMigrator;
    private JobConfigMigrator jobMigrator;
    private ItemWithTemplateConfigMigrator templateMigrator;
    private ItemConfigMigrator migrator;
    
    private Jenkins jenkins;
    
    @Rule
    public JenkinsRule j = new JenkinsRule();
    
    @Before
    public void setUp() {
        jenkins = j.getInstance();
        
        projectMigrator = mock(AbstractProjectConfigMigrator.class);
        jobMigrator = mock(JobConfigMigrator.class);
        templateMigrator = mock(ItemWithTemplateConfigMigrator.class);
        
        migrator = new ItemConfigMigrator(projectMigrator, jobMigrator, templateMigrator);
    }

    @Test
    public void testMigrate_AbstractProject() {
        AbstractProject<?, ?> item = mock(AbstractProject.class);
        
        assertTrue(migrator.migrate(item));
        
        verify(projectMigrator).migrate(any(AbstractProject.class));
        verify(jobMigrator, never()).migrate(any(Job.class));
        verify(templateMigrator, never()).migrate(any(AbstractProject.class));
    }
    
    @Test
    public void testMigrate_Job() {
        Job<?, ?> item = mock(Job.class);
        
        assertTrue(migrator.migrate(item));
        
        verify(projectMigrator, never()).migrate(any(AbstractProject.class));
        verify(jobMigrator).migrate(any(Job.class));
        verify(templateMigrator, never()).migrate(any(AbstractProject.class));
    }
    
    @Test
    public void testMigrate_JobWithPublishersList() {
        Job<?, ?> item = new JobWithPublishers(jenkins, "Random Name");
        
        doReturn(false).when(templateMigrator).migrate(item);
        
        assertFalse(migrator.migrate(item));
        
        verify(projectMigrator, never()).migrate(any(AbstractProject.class));
        verify(jobMigrator, never()).migrate(any(Job.class));
        verify(templateMigrator).migrate(any(AbstractProject.class));
    }
    
    @Test
    public void testMigrate_ItemWithTemplate() {
        
        Item item = mock(Item.class);
        
        doReturn(true).when(templateMigrator).migrate(item);
        
        assertTrue(migrator.migrate(item));
        
        verify(projectMigrator, never()).migrate(any(AbstractProject.class));
        verify(jobMigrator, never()).migrate(any(Job.class));
        verify(templateMigrator).migrate(eq(item));
    }
    
    @SuppressWarnings("rawtypes")
    private static class JobWithPublishers extends ViewJob {
        
        public JobWithPublishers(ItemGroup parent, String name) {
            super(parent, name);
        }

        @Override
        public void reload() {
        }
        
        @SuppressWarnings("unused")
        public DescribableList<Publisher,Descriptor<Publisher>> getPublishersList() {
            return null;
        }
    }
}
