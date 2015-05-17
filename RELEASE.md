# Steps to release

This outlines the maintainers steps to release the Jenkins Slack plugin.  Follow
the Jenkins documentation for [making a new release][plugin-release].

1. Configure your credentials in `~/.m2/settings.xml`. (outlined in [making a
   new release][plugin-release] doc)
1. Create a new issue to track the release and give it the label
   `maintainer communication`.
1. Create a release branch. `git checkout origin/master -b prepare_release`
1. Update the release notes in `CHANGELOG.md`.
1. Open a pull request from `prepare_release` branch to `master` branch.  Merge
   it.
1. Fetch the latest `master`.
1. Execute the release plugin.

    mvn org.apache.maven.plugins:maven-release-plugin:2.5:prepare org.apache.maven.plugins:maven-release-plugin:2.5:perform

I pin which version of the release plugin to use because of the working around
common issues section of the [release document][plugin-release].


[plugin-release]: https://wiki.jenkins-ci.org/display/JENKINS/Hosting+Plugins
