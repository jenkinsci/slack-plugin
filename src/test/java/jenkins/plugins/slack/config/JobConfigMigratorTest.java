package jenkins.plugins.slack.config;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import hudson.model.Job;

import java.io.IOException;

import jenkins.plugins.slack.SlackNotifier.SlackJobProperty;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class JobConfigMigratorTest {
    
    private JobConfigMigrator migrator;
    
    @Before
    public void setUp() {
        migrator = new JobConfigMigrator();
    }

    @Test
    public void testMigrate_WithNoSlackJobProperty() throws Exception {
        Job<?, ?> project = mock(Job.class);
        migrator.migrate(project);
        verify(project).getProperty(eq(SlackJobProperty.class));
        verify(project, never()).removeProperty(eq(SlackJobProperty.class));
    }
    
    @Test
    public void testMigrate_WithSlackJobProperty() throws Exception {
        SlackJobProperty jobProperty = mock(SlackJobProperty.class);
        
        Job<?, ?> project = mock(Job.class);
        when(project.getProperty(eq(SlackJobProperty.class))).thenReturn(jobProperty);
        
        migrator.migrate(project);
        verify(project).getProperty(eq(SlackJobProperty.class));
        verify(project).removeProperty(eq(SlackJobProperty.class));
    }
    
    @Test
    public void testMigrate_WithIOExceptionOnSave() throws Exception {
        SlackJobProperty jobProperty = mock(SlackJobProperty.class);
        
        Job<?, ?> project = mock(Job.class);
        when(project.getProperty(eq(SlackJobProperty.class))).thenReturn(jobProperty);
        doThrow(new IOException("Something bad happened")).when(project).save();
        
        migrator.migrate(project);
        
        // exception caught and nothing bad happened
    }
}
