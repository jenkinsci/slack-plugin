# Slack plugin for Jenkins

Started with a fork of the HipChat plugin:

https://github.com/jlewallen/jenkins-hipchat-plugin

Which was, in turn, a fork of the Campfire plugin.

# Jenkins Instructions

1. Get a Slack account: https://slack.com/
2. Configure the Jenkins integration: https://my.slack.com/services/new/jenkins-ci
3. Install this plugin on your Jenkins server

# Developer instructions

Install Maven and JDK.  This was last build with Maven 2.2.1 and OpenJDK
1.7.0\_75 on Ubuntu 14.04.

Run unit tests

    mvn test

Create a JAR file to install in Jenkins (JAR file will be in `target/slack.jar`).

    mvn package
