package jenkins.plugins.slack.webhook;


import hudson.Extension;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;


@Extension
public class GlobalConfig extends GlobalConfiguration {

    private String slackOutgoingWebhookToken;
    private String slackOutgoingWebhookURL;

    public GlobalConfig() {
        load();
    }

    public String getSlackOutgoingWebhookToken() {
        return slackOutgoingWebhookToken;
    }

    public void setSlackOutgoingWebhookToken(String slackOutgoingWebhookToken) {
        this.slackOutgoingWebhookToken = slackOutgoingWebhookToken;
    }

    public FormValidation doCheckSlackOutgoingWebhookToken(@QueryParameter String value) {
        return FormValidation.ok();
    }

    public String getSlackOutgoingWebhookURL() {
        return slackOutgoingWebhookURL;
    }

    public void setSlackOutgoingWebhookURL(String slackOutgoingWebhookURL) {
        this.slackOutgoingWebhookURL = slackOutgoingWebhookURL.replaceFirst("^/", "");
    }

    public FormValidation doCheckSlackOutgoingWebhookURL(@QueryParameter String value) {
        return FormValidation.ok();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) {
        req.bindJSON(this, json);
        save();
        return true;
    }
}
