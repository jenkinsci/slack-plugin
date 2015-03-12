# **Curated** Slack plugin for Jenkins-CI - [![Build Status](https://travis-ci.org/peergum/slack-plugin.svg?branch=master)](https://travis-ci.org/peergum/slack-plugin)

This plugin is a fork of the official Jenkins-Slack plugin, itself a fork of the [HipChat plugin](https://github.com/jlewallen/jenkins-hipchat-plugin), which was, in turn, a fork of the Campfire plugin.

##Note
For the note, this plugin was forked from the original Slack plugin due to divergences of views about the way new features and changes were implemented into the plugin without any consensus from the user base, potentially compatibility breaks and causing issues for some, myself included. This particular repository will implement mostly corrective patches and improvements, but any new feature or change impacting notifications from the plugin will be first voted and will require at least 50% of agreement from the participants. The repository will be added to Jenkins official library as soon as a reasonable number of downloads will be reached, showing interest.

# Jenkins Instructions

1. Get a Slack account: https://slack.com/
2. Configure the Jenkins integration: https://my.slack.com/services/new/jenkins-ci
3. Install this plugin on your Jenkins server
4. Go to Jenkins general settings and add your slack team name, API token, preferred notification channel(s) and desired notification settings
5. Go to each job you want to notify about, go to the configuration, add a Post Build step and configure a specific team, API token and channel(s) if necessary, otherwise default values will be used.

# Developer instructions

Install Maven and JDK.  This was last build with Maven 3.2.5 and OpenJDK
1.8 on a Mac under Netbeans 8.0.2

Run unit tests

    mvn test

Create an HPI file to install in Jenkins (HPI file will be in `target/slack.hpi`).

    mvn package
