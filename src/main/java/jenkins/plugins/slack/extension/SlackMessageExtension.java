package jenkins.plugins.slack.extension;

import hudson.ExtensionPoint;

public abstract class SlackMessageExtension implements ExtensionPoint {

    public String doReplacement(String message) {
        return message;
    }

}
