package jenkins.plugins.slack.logging;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

public class BuildKey {
    private static final String UNKNOWN = "[UNKNOWN BUILD]";

    public static String format(AbstractBuild<?, ?> build) {
        if (build == null) {
            return UNKNOWN;
        }
        AbstractProject<?, ?> project = build.getProject();
        if (project == null) {
            return UNKNOWN;
        }
        return "[" + project.getFullDisplayName() + " #" + build.getNumber() + "]";
    }
}
