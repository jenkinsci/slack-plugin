package jenkins.plugins.slack;

import hudson.model.AbstractBuild;

public interface TokenExpander {
    String expand(String template, AbstractBuild<?, ?> build);
}
