# Slack plugin for Jenkins

[![Build Status](https://ci.jenkins.io/buildStatus/icon?job=Plugins/slack-plugin/master)](https://ci.jenkins.io/job/Plugins/slack-plugin/master)
[![Slack Signup][slack-badge]][slack-signup] (click to sign up)

Provides Jenkins notification integration with Slack or Slack compatible
applications like [RocketChat][rocketchat] and [Mattermost][mattermost].

## Install Instructions for Slack

1. Get a Slack account: https://slack.com/
2. Configure the Jenkins integration:
   https://my.slack.com/services/new/jenkins-ci
3. Install this plugin on your Jenkins server.

### Freestyle job
1. Configure it in your Jenkins job (and optionally as global configuration) and
   **add it as a Post-build action**.
   
### Pipeline job

```
slackSend color: 'good', message: 'Message from Jenkins Pipeline'
```

Additionally you can pass a JSONArray as a String in order to send complex
messages, as per the example:

```
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
node {
    JSONArray attachments = new JSONArray();
    JSONObject attachment = new JSONObject();

    attachment.put('text','I find your lack of faith disturbing!');
    attachment.put('fallback','Hey, Vader seems to be mad at you.');
    attachment.put('color','#ff0000');

    attachments.add(attachment);
    slackSend(color: '#00FF00', channel: '@gustavo.maia', attachments: attachments.toString())
}
```
For more information about slack messages see [Slack Messages Api](https://api.slack.com/docs/messages)
and [Slack attachments Api](https://api.slack.com/docs/message-attachments)

#### Threads Support

You can send a message and create a thread on that message using the pipeline step.
The step returns an object which you can use to retrieve the thread ID. Send new messages with that thread ID as the
target channel to create a thread. All messages of a thread should use the same thread ID.

Example:
```
node {
    def slackResponse = slackSend(channel: "cool-threads", message: "Here is the primary message")
    slackSend(channel: slackResponse.threadId, message: "Thread reply #1")
    slackSend(channel: slackResponse.threadId, message: "Thread reply #2")
}
```

This feature requires botUser mode.


## Install Instructions for Slack compatible application

1. Log into Slack compatible application.
2. Create a Webhook (it may need to be enabled in system console) by visiting
   Integrations.
3. You should now have a URL with a token.  Something like
   `https://mydomain.com/hooks/xxxx` where `xxxx` is the integration token and
   `https://mydomain.com/hooks/` is the `Slack compatible app URL`.
4. Install this plugin on your Jenkins server.
5. Configure it in your Jenkins job (and optionally as global configuration) and
   **add it as a Post-build action**.

If you want to configure a notification to be sent to Slack for **all jobs**, you may want to also consider installing an additional plugin called [Global Slack Notifier plugin](https://github.com/jenkinsci/global-slack-notifier-plugin).

## Security

Use Jenkins Credentials and a credential ID to configure the Slack integration
token. It is a security risk to expose your integration token using the previous
*Integration Token* setting.

Create a new ***Secret text*** credential:

![image][img-secret-text]


Select that credential as the value for the ***Integration Token Credential
ID*** field:

![image][img-token-credential]

## Direct Message

You can send messages to channels or you can notify individual users via their
slackbot.  In order to notify an individual user, use the syntax `@user_id` in
place of the project channel.  Mentioning users by display name may work, but it
is not unique and will not work if it is an ambiguous match.    

## Bot user option

This plugin supports sending notifications via bot users. You can enable bot
user support from both global and project configurations. If the notification
will be sent to a user via direct message, default integration sends it via
@slackbot, you can use this option if you want to send messages via a bot user.
You need to provide credentials of the bot user for integration token
credentials to use this feature.

Bot user option is not supported, if you use Base Url for a Slack compatible
application.

## Configuration as code

This plugin supports configuration as code
Add to your yaml file:
```yaml
credentials:
  system:
    domainCredentials:
      - credentials:
          - string:
              scope: GLOBAL
              id: slack-token
              secret: '${SLACK_TOKEN}'
              description: Slack token


unclassified:
  slackNotifier:
    teamDomain: <your-slack-workspace-name> # i.e. your-company (just the workspace name not the full url)
    tokenCredentialId: slack-token
```

## Developer instructions

Install Maven and JDK.

```
$ mvn -version | grep -v home
Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T08:41:47-08:00)
Java version: 1.7.0_79, vendor: Oracle Corporation
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "4.4.0-65-generic", arch: "amd64", family: "unix"
```

Run unit tests

    mvn test

Create an HPI file to install in Jenkins (HPI file will be in
`target/slack.hpi`).

    mvn clean package

[jenkins-builds]: https://jenkins.ci.cloudbees.com/job/plugins/job/slack-plugin/
[jenkins-status]: https://jenkins.ci.cloudbees.com/buildStatus/icon?job=plugins/slack-plugin
[slack-badge]: https://jenkins-slack-testing-signup.herokuapp.com/badge.svg
[slack-signup]: https://jenkins-slack-testing-signup.herokuapp.com/
[rocketchat]: https://rocket.chat/
[mattermost]: https://about.mattermost.com/
[img-secret-text]: https://cloud.githubusercontent.com/assets/983526/17971588/6c26dfa0-6aa9-11e6-808c-3e139446e013.png
[img-token-credential]: https://cloud.githubusercontent.com/assets/983526/17971458/ec296bf6-6aa8-11e6-8d19-06d9f1c9d611.png
