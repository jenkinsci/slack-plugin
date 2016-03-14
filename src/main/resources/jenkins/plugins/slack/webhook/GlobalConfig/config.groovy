package jenkins.plugins.slack.webhook.GlobalConfig;

f = namespace('/lib/form')

f.section(title: _('Slack Webhook Settings')) {
    f.entry(field: 'slackOutgoingWebhookToken', title: _('Outgoing Webhook Token')) {
        f.textbox()
    }
    f.entry(field: 'slackOutgoingWebhookURL', title: _('Outgoing Webhook URL Endpoint')) {
        f.textbox()
    }
}
