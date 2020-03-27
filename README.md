# Slack plugin for Jenkins

[![Build Status][jenkins-status]][jenkins-builds]
[![Jenkins Plugin][plugin-version-badge]][plugin]
[![GitHub release][github-release-badge]][github-release]
[![Jenkins Plugin Installs][plugin-install-badge]][plugin]
[![Slack Signup][slack-badge]][slack-signup] (click to sign up)

Provides Jenkins notification integration with Slack or Slack compatible
applications like [RocketChat][rocketchat] and [Mattermost][mattermost].

## Install Instructions for Slack

1.  Get a Slack account: <https://slack.com/>
2.  Configure the Jenkins integration: <https://my.slack.com/services/new/jenkins-ci>
3.  Install this plugin on your Jenkins server:

    1.  From the Jenkins homepage navigate to `Manage Jenkins`
    2.  Navigate to `Manage Plugins`,
    3.  Change the tab to `Available`,
    4.  Search for `slack`,
    5.  Check the box next to install.

![image][img-plugin-manager]

If you want to configure a notification to be sent to Slack for **all jobs**, you may want to also consider installing an additional plugin called [Global Slack Notifier plugin](https://plugins.jenkins.io/global-slack-notifier).

### Pipeline job

    slackSend color: 'good', message: 'Message from Jenkins Pipeline'

Additionally you can pass attachments or blocks (requires [bot user](#bot-user-mode)) in order to send complex
messages, for example:

Attachments:

```groovy
def attachments = [
  [
    text: 'I find your lack of faith disturbing!',
    fallback: 'Hey, Vader seems to be mad at you.',
    color: '#ff0000'
  ]
]

slackSend(channel: '#general', attachments: attachments)
```

Blocks (this feature requires a '[bot user](#bot-user-mode)' and a custom slack app):

```groovy
blocks = [
	[
		"type": "section",
		"text": [
			"type": "mrkdwn",
			"text": "Hello, Assistant to the Regional Manager Dwight! *Michael Scott* wants to know where you'd like to take the Paper Company investors to dinner tonight.\n\n *Please select a restaurant:*"
		]
	],
    [
		"type": "divider"
	],
	[
		"type": "section",
		"text": [
			"type": "mrkdwn",
			"text": "*Farmhouse Thai Cuisine*\n:star::star::star::star: 1528 reviews\n They do have some vegan options, like the roti and curry, plus they have a ton of salad stuff and noodles can be ordered without meat!! They have something for everyone here"
		],
		"accessory": [
			"type": "image",
			"image_url": "https://s3-media3.fl.yelpcdn.com/bphoto/c7ed05m9lC2EmA3Aruue7A/o.jpg",
			"alt_text": "alt text for image"
		]
	]
]

slackSend(channel: '#general', blocks: blocks)
```

For more information about slack messages see [Slack Messages Api](https://api.slack.com/docs/messages), [Slack attachments Api](https://api.slack.com/docs/message-attachments) and [Block kit](https://api.slack.com/block-kit)

Note: the attachments API is classified as legacy, with blocks as the replacement (but blocks are only supported when using a bot user through a custom slack app).

#### File upload

You can upload files to slack with this plugin:

```groovy
node {
  sh "echo hey > blah.txt"
  slackUploadFile filePath: '*.txt', initialComment:  'HEY HEY'
}
```

This feature requires [botUser](#bot-user-mode) mode.

#### Threads Support

You can send a message and create a thread on that message using the pipeline step.
The step returns an object which you can use to retrieve the thread ID. Send new messages with that thread ID as the
target channel to create a thread. All messages of a thread should use the same thread ID.

Example:

```groovy
def slackResponse = slackSend(channel: "cool-threads", message: "Here is the primary message")
slackSend(channel: slackResponse.threadId, message: "Thread reply #1")
slackSend(channel: slackResponse.threadId, message: "Thread reply #2")
```

This feature requires [botUser](#bot-user-mode) mode.

Messages that are posted to a thread can also optionally be broadcasted to the
channel. Set `replyBroadcast: true` to do so. For example:

```groovy
def slackResponse = slackSend(channel: "ci", message: "Started build")
slackSend(channel: slackResponse.threadId, message: "Build still in progress")
slackSend(
    channel: slackResponse.threadId,
    replyBroadcast: true,
    message: "Build failed. Broadcast to channel for better visibility."
)
```

#### Update Messages

You can update the content of a previously sent message using the pipeline step.
The step returns an object which you can use to retrieve the timestamp and channelId
NOTE: The slack API requires the channel ID for `chat.update` calls.

Example:

```groovy
def slackResponse = slackSend(channel: "updating-stuff", message: "Here is the primary message")
slackSend(channel: slackResponse.channelId, message: "Update message now", timestamp: slackResponse.ts)
```

This feature requires [botUser](#bot-user-mode) mode.

#### Emoji Reactions

Add an emoji reaction to a previously-sent message like this:

Example:

```groovy
def slackResponse = slackSend(channel: "emoji-demo", message: "Here is the primary message")
slackResponse.addReaction("thumbsup")
```

This may only work reliably in channels (as opposed to private messages) due to [limitations in the Slack API](https://api.slack.com/methods/chat.postMessage) (See "Post to an IM channel").

#### Unfurling Links

You can allow link unfurling if you send the message as text. This only works in a text message, as attachments cannot be unfurled.

Example:

```groovy
slackSend(channel: "news-update", message: "https://www.nytimes.com", sendAsText: true)
```

#### User Id Look Up

There are two pipeline steps available to help with user id look up.

A user id can be resolved from a user's email address with the `slackUserIdFromEmail` step.

Example:

```groovy
def userId = slackUserIdFromEmail('spengler@ghostbusters.example.com')
slackSend(color: 'good', message: "<@$userId> Message from Jenkins Pipeline")
```

A list of user ids can be resolved against the set of changeset commit authors with the `slackUserIdsFromCommitters` step.

Example:

```groovy
def userIds = slackUserIdsFromCommitters()
def userIdsString = userIds.collect { "<@$it>" }.join(' ')
slackSend(color: 'good', message: "$userIds Message from Jenkins Pipeline")
```

This feature requires [botUser](#bot-user-mode) mode.

### Freestyle job

1.  Configure it in your Jenkins job (and optionally as global configuration) and
    **add it as a Post-build action**.

## Install Instructions for Slack compatible application

1.  Log into the Slack compatible application.
2.  Create a Webhook (it may need to be enabled in system console) by visiting
    Integrations.
3.  You should now have a URL with a token.  Something like
    `https://mydomain.com/hooks/xxxx` where `xxxx` is the integration token and
    `https://mydomain.com/hooks/` is the `Slack compatible app URL`.
4.  Install this plugin on your Jenkins server.
5.  Follow the freestyle or pipeline instructions for the slack installation instructions.

## Security

Use Jenkins Credentials and a credential ID to configure the Slack integration
token. It is a security risk to expose your integration token using the previous
_Integration Token_ setting.

Create a new **_Secret text_** credential:

![image][img-secret-text]

Select that credential as the value for the **_Credential_** field:

![image][img-token-credential]

## Direct Message

You can send messages to channels or you can notify individual users via their
slackbot.  In order to notify an individual user, use the syntax `@user_id` in
place of the project channel.  Mentioning users by display name may work, but it
is not unique and will not work if it is an ambiguous match.

## User Mentions

Use the syntax `<@user_id>` in a message to mention users directly. See [User Id Look Up](#user-id-look-up) for pipeline steps to help with user id look up.

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

For more details see the configuration as code plugin documentation:
<https://github.com/jenkinsci/configuration-as-code-plugin#getting-started>

## Bot user mode

There's two ways to authenticate with slack using this plugin.

1.  Using the "Jenkins CI" app written by Slack,
    it's what is known as a 'legacy app' written directly into the slack code base and not maintained anymore.

2.  Creating your own custom "Slack app" and installing it to your workspace.

The benefit of using your own custom "Slack app" is that you get to use all of the modern features that Slack has released in the last few years to
Slack apps and not to legacy apps.

These include:

-   Threading
-   File upload
-   Custom app emoji per message
-   Blocks

The bot user option is not supported if you use the _Slack compatible app URL_
option.

### Creating your app

Note: These docs may become outdated as Slack changes their website, if they do become outdated please send a PR here to update the docs.

1.  Go to <https://api.slack.com/> and click "Start building".
2.  Pick an app name, i.e. "Jenkins" and a workspace that you'll be installing it to.
3.  Go to basic information and set the default icon in "Display information" you can get the Jenkins logo from: <https://jenkins.io/artwork/>.
4.  Navigate to "OAuth & Permissions".
5.  Add `chat:write` OAuth Scope to "Bot Token Scopes".
6.  Install the app to a workspace to generate "Bot User OAuth Access Token".
7.  Copy the "Bot User OAuth Access Token" and create a "Secret text" credential in Jenkins with this.
8.  Tick the "Is Bot User?" option in the Slack configuration in "Manage Jenkins".
9.  Invite the Jenkins bot user into the Slack channel(s) you wish to be notified in.
10.  Click test connection.

## Troubleshooting connection failure

When testing the connection, you may see errors like:

```text
    WARNING j.p.slack.StandardSlackService#publish: Response Code: 404
```

There's a couple of things to try:

### Have you enabled bot user mode?

If you've ticked `Custom slack app bot user` then try unticking it, that mode is for when you've created a custom app and installed it to your workspace instead of the default Jenkins app made by Slack

### Have you set the override URL?

If you've entered something into `Override url` then try clearing it out, that field is only needed for slack compatible apps like mattermost.

### Enable additional logging

Add a [log recorder](https://support.cloudbees.com/hc/en-us/articles/204880580-How-do-I-create-a-logger-in-Jenkins-for-troubleshooting-and-diagnostic-information-) for the [StandardSlackService](https://github.com/jenkinsci/slack-plugin/blob/master/src/main/java/jenkins/plugins/slack/StandardSlackService.java) class this should give you additional details on what's going on.

If you still can't figure it out please raise an issue with as much information as possible about your config and any relevant logs.

## Developer instructions

Install Maven and JDK.

```shell
$ mvn -version | grep -v home
Apache Maven 3.3.9 (bb52d8502b132ec0a5a3f4c09453c07478323dc5; 2015-11-10T08:41:47-08:00)
Java version: 1.7.0_79, vendor: Oracle Corporation
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "4.4.0-65-generic", arch: "amd64", family: "unix"
```

Run unit tests

```shell
mvn test
```

Create an HPI file to install in Jenkins (HPI file will be in
`target/slack.hpi`).

```shell
mvn clean package
```

[jenkins-builds]: https://ci.jenkins.io/job/Plugins/job/slack-plugin/job/master/

[jenkins-status]: https://ci.jenkins.io/buildStatus/icon?job=Plugins/slack-plugin/master

[slack-badge]: https://jenkins-slack-testing-signup.herokuapp.com/badge.svg

[slack-signup]: https://jenkins-slack-testing-signup.herokuapp.com/

[plugin-version-badge]: https://img.shields.io/jenkins/plugin/v/slack.svg

[plugin-install-badge]: https://img.shields.io/jenkins/plugin/i/slack.svg?color=blue

[plugin]: https://plugins.jenkins.io/slack

[github-release-badge]: https://img.shields.io/github/release/jenkinsci/slack-plugin.svg?label=release

[github-release]: https://github.com/jenkinsci/slack-plugin/releases/latest

[rocketchat]: https://rocket.chat/

[mattermost]: https://about.mattermost.com/

[img-secret-text]: https://cloud.githubusercontent.com/assets/983526/17971588/6c26dfa0-6aa9-11e6-808c-3e139446e013.png

[img-token-credential]: /docs/global-config.png

[img-plugin-manager]: /docs/plugin-manager-search.png
