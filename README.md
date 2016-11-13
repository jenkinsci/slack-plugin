Slack plugin for Jenkins  [![Build Status][jenkins-status]][jenkins-builds] [![Slack Signup][slack-badge]][slack-signup]
----------------------------------------------------------------

Provides Jenkins notification integration with Slack.

## Install Instructions

1. Get a Slack account: https://slack.com/
2. Configure the Jenkins integration: https://my.slack.com/services/new/jenkins-ci
3. Install this plugin on your Jenkins server
4. Configure it in your Jenkins job (and optionally as global configuration) and **add it as a Post-build action**.

#### Security

Use Jenkins Credentials and a credential ID to configure the Slack integration token. It is a security risk to expose your integration token using the previous *Integration Token* setting.

Create a new ***Secret text*** credential:
![image](https://cloud.githubusercontent.com/assets/983526/17971588/6c26dfa0-6aa9-11e6-808c-3e139446e013.png)


Select that credential as the value for the ***Integration Token Credential ID*** field:
![image](https://cloud.githubusercontent.com/assets/983526/17971458/ec296bf6-6aa8-11e6-8d19-06d9f1c9d611.png)

#### Jenkins Pipeline Support

Includes [Jenkins Pipeline](https://github.com/jenkinsci/workflow-plugin) support as of version 2.0:

```
slackSend color: 'good', message: 'Message from Jenkins Pipeline'
```

### Developer instructions

Install Maven and JDK.  This was last build with Maven 3.2.5 and OpenJDK
1.7.0\_75 on KUbuntu 14.04.

Run unit tests

    mvn test

Create an HPI file to install in Jenkins (HPI file will be in `target/slack.hpi`).

    mvn package

[jenkins-builds]: https://jenkins.ci.cloudbees.com/job/plugins/job/slack-plugin/
[jenkins-status]: https://jenkins.ci.cloudbees.com/buildStatus/icon?job=plugins/slack-plugin
[slack-badge]: https://jenkins-slack-testing-signup.herokuapp.com/badge.svg
[slack-signup]: https://jenkins-slack-testing-signup.herokuapp.com/
