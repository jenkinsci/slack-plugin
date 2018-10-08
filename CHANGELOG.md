# 2.4 release notes

## New Features:

- Support JCasC ([#404][#404]) [JENKINS-53641](https://issues.jenkins-ci.org/browse/JENKINS-53641)
  - [example](/README.md#configuration-as-code)
- Add Token Macro support ([#401][#401])

## Bug Fixes:

- [JENKINS-50706] Fix permission check for credentials checkbox ([#372][#372])
- [JENKINS-37339] Prevent NPE after plugin installation ([#403][#403])
- [JENKINS-53450] Rename the variable that fails the tests on Windows ([#394][#394])
- added @DataBoundSetter for includeTestSummary ([#393][#393])
- Reduce verbosity for standard case when the migrator does nothing ([#395][#395])
- Updated docs - Added multiple channel comment to slackSend ([#399][#399])
- Create Jenkinsfile ([#405][#405])
- slack outgoing webhook isn't working [PR #344](https://github.com/jenkinsci/slack-plugin/pull/344)

## Security Related:

- jackson-databind -> 2.8.11.2, security fix ([#400][#400])

# 2.2 release notes

New Features:

- Implement bot-user support ([#258][#258])
- Use DataBoundSetter for optional params ([#236][#236], [#232][#232])
- Add `Include failed Tests` ([#166][#166])
- Add display failed tests option to slack notifications on job configuration
  ([#275][#275])
- Add baseUrl parameter to support slack-compatible integrations like Mattermost
  or Rocket Chat ([#293][#293])
- Regression notification implemented ([#294][#294])

Bug Fixes:

- Fix #126 Replace `<a>` tags with Slack `<link|name>` ([#222][#222],
  [#126][#126])
- Fix #188 Fixes for configuration migration from 1.8 to 2.0 ([#202][#202],
  [#188][#188])
- Changed calculation of `Back to normal` time ([#228][#228], [#225][#225])
- Only send `Back to normal` if `Notify Back To Normal` is enabled
  ([#164][#164])
- Add custom message for manually triggered builds on build start ([#162][#162])
- Fix #62 by excluding empty affected files collection ([#197][#197],
  [#62][#62])
- Moved message to `text` instead of `fields` to resolve truncated message issue
  ([#274][#274])

[#126]: https://github.com/jenkinsci/slack-plugin/issues/126
[#162]: https://github.com/jenkinsci/slack-plugin/issues/162
[#164]: https://github.com/jenkinsci/slack-plugin/issues/164
[#166]: https://github.com/jenkinsci/slack-plugin/issues/166
[#188]: https://github.com/jenkinsci/slack-plugin/issues/188
[#197]: https://github.com/jenkinsci/slack-plugin/issues/197
[#202]: https://github.com/jenkinsci/slack-plugin/issues/202
[#222]: https://github.com/jenkinsci/slack-plugin/issues/222
[#225]: https://github.com/jenkinsci/slack-plugin/issues/225
[#228]: https://github.com/jenkinsci/slack-plugin/issues/228
[#232]: https://github.com/jenkinsci/slack-plugin/issues/232
[#236]: https://github.com/jenkinsci/slack-plugin/issues/236
[#258]: https://github.com/jenkinsci/slack-plugin/issues/258
[#274]: https://github.com/jenkinsci/slack-plugin/issues/274
[#275]: https://github.com/jenkinsci/slack-plugin/issues/275
[#293]: https://github.com/jenkinsci/slack-plugin/issues/293
[#294]: https://github.com/jenkinsci/slack-plugin/issues/294
[#62]: https://github.com/jenkinsci/slack-plugin/issues/62

# 2.1 release notes

New Features:

- Added Jenkins Credentials support (#247)
- Added support for display-url-api; will allow Blue Ocean to change display
  URLs (#245)
- Added support for @here and other @mentions (#241)
- Reduced log verbosity (#252)
- Updated `@Extension` ordinal value to force earlier migration process (#261)

UI Improvements:

-  Added credentials selection widget (#247)

Bug Fixes:

- Fixes for Security issue [JENKINS-35503][JENKINS-35503] (#247)
- Fixed documentation for Pipeline step (#220)

[JENKINS-35503]: https://issues.jenkins-ci.org/browse/JENKINS-35503

# 2.0.1 release notes

Bug Fixes:

- Set webhook endpoint to random uuid based string if not set (PR # 190)
- Upgrading to Slack 2.0 outbound webhooks breaks Jenkins  (#191)

# 2.0 release notes

New Features:

- Adding configurable webhook endpoint for exposing jenkins commands with a
  Slack outgoing-webhook (#160)
- Added Jenkins Pipeline support via custom step `slackSend`. (#167)

UI Improvements:

- Job level configuration was moved under post-build actions with support for
  migration from previous versions. (#79, #170)
- Use Subdomain instead of Domain (#168)

Bug Fixes

- Notify for repeated failures doesn't work (#136)
- Workflow plugin integration (#50)
- "Back to normal" message duration is build duration rather than time the job
  has been broken for (#129)
- [FIXED JENKINS-30559] Dropdown list to select commit info for notifications
  (#133)
- Fix double start message when build is manually started (#96, #137)
- "success" instead of "back to normal" after aborted build (123)
- Doesn't support the Jenkins Inheritance plugin (#6)


# 1.8.1 release notes

This is a backport release which backports features originally intended for
slack-2.0 release.  This is meant as a nice fix for people missing critical
bugs.  Thanks [@Nooba](https://github.com/Nooba/) for taking the time to
backport all of the changes.  Since new features listed here will also be
included in the slack-2.0 release I'm not going to bother mentioning them in the
slack-2.0 release at all.

New Features:

- Allow simple markup formatting in custom messages. (#127)

UI Improvements:

- Improves custom message help text. (#97)

Bug fixes:

- Allow parallel builds. (#122)
- Use textarea for customMessage.  This allows custom messages to have new
  lines. (#103)
- Set test connection message color correctly. (#101)
- Fix publish to multiple rooms. Add unit tests. (#98, #100)
- Include custom message when changes are found. (#95)

# 1.8.0 release notes

New Features:

- Option to add a custom message (#49, #78)
- Build Server URL defaults to Jenkins URL when the slack plugin is first
  configured (#42, #90)

UI Improvements:

- Move `Test Connection` button in slack job config to the advanced section
  (#64)
- Improvements to `Test Connection` button.  It now provides feedback in the
  Jenkins UI when it succeeds or fails to connect to the slack instance (#51,
  #81)

Improvements:

- Improve 'started' message with proper cause (#37)
- The separator for specifying posting to multiple slack channels is more
  robust.  It now allows spaces, commas, and semicolons as a separator (#56)
- Notifications start with full project name when using Cloudbees folders
  plugin.  This includes the folder and the project in the notification (#61)
- Search upstream causes when gathering commit list (#67)
- Change repeated failure message improvement with `Still Failing`.  Makes it
  easier to understand if it is the first or repeated failure in a build (#77)
- Add unit tests for `doTestConnection` method (#82, #84)

Bug fixes:

- Fix `Include Test Summary` and `Notify Repeated Failure` options being
  reversed when saving settings in the UI (#63)
- Change `.getPreviousBuild()` to allow for higher concurrency (#70)
- Solve `java.lang.NoClassDefFoundError` when running unit tests (#82, #83)
- Adding ability for environment variables and parameters (#31, #80, #89)

# 1.7.0 release notes

New features:

- Advanced settings
  - Team domain and token can be specified per project; falls back to global
    config if not specified (#19)
  - Post a list of commits with the build notification (title and author of
    commit) (#30, #44, #45)
  - Include JUnit test summary in build notifications (#25)
- Use colors when sending a message using slack (#20, #24).  Also, the start
  notifications use the color of the last build result (#43)
- Support for authenticated proxies (#14)
- Test Connection button (#46, #28)
- Option to disable subsequent notifications on build failures (#46, #15)

Improvements:

- Report 'return to normal' on transition from unstable to success (#21).
- Improved logging.

Bug fixes:

- When changing the global slack settings the slack config in jobs are updated
  as well (#26, #12)
- Fix NullPointerException and output log message if slack is not configured
  (#35, JENKINS-26066)
