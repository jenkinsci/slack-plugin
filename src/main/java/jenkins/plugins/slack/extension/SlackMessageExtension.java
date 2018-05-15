package jenkins.plugins.slack.extension;

import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;

public abstract class SlackMessageExtension implements ExtensionPoint {

    public String doReplacement(String message, AbstractBuild build) {
        return message;
    }

}
