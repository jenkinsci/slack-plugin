# Changelog

## 2.16 (unreleased) release notes

### Bug fixes:

- java.lang.NumberFormatException: When using parametrized scheduler [PR #510](https://github.com/jenkinsci/slack-plugin/pull/510)

## 2.15 release notes (2019-01-01)

### Removed functionality

- Integration token text removed from global configuration, it will be automatically migrated into a credential for you. There's upcoming changes for jobs and pipelines in this area as well [PR #493](https://github.com/jenkinsci/slack-plugin/pull/493)

### Bug fixes

- Add null check to extractReplaceLinks [PR #497](https://github.com/jenkinsci/slack-plugin/pull/497)

### New features

- Log response string to assist user [PR #486](https://github.com/jenkinsci/slack-plugin/pull/486)
- Add class name to failed test notification [PR #474](https://github.com/jenkinsci/slack-plugin/pull/474)

### Internal

- Remove overriding plugin pom config [PR #492](https://github.com/jenkinsci/slack-plugin/pull/492)
- Remove deprecated code [PR #491](https://github.com/jenkinsci/slack-plugin/pull/491)
- Bump baseline and parent pom [PR #485](https://github.com/jenkinsci/slack-plugin/pull/485)
- IntelliJ automatic code cleanup [PR #484](https://github.com/jenkinsci/slack-plugin/pull/484)
- Remove token botUser hack [PR #483](https://github.com/jenkinsci/slack-plugin/pull/483)

## 2.14 release notes

### New features:

- Allow broadcasting thread messages to the channel [PR #473](https://github.com/jenkinsci/slack-plugin/pull/473)

### Bug fixes:

- Fix UI fields [PR #471](https://github.com/jenkinsci/slack-plugin/pull/471)

## 2.13 release notes

### Bug fixes:

- Fixed constructor of SlackJobProperty [PR #468](https://github.com/jenkinsci/slack-plugin/pull/468)
- Adding null check on custom message [PR #466](https://github.com/jenkinsci/slack-plugin/pull/466)
- Always runAfterFinalized [PR #462](https://github.com/jenkinsci/slack-plugin/pull/462)

### Docs updates:

- Added note for Global Slack Notifications [PR #470](https://github.com/jenkinsci/slack-plugin/pull/470)

## 2.12 release notes

### Bug fixes:

- Revert 'Don't publish twice, while the build isn't finished yet' [PR #457](https://github.com/jenkinsci/slack-plugin/pull/457)

## 2.11 release notes

### Bug fixes:

- Move optional params to setters and fix constructor compatibility [PR #452](https://github.com/jenkinsci/slack-plugin/pull/452)

## 2.10 release notes

### New features:

- Send only one message with commit list [PR #448](https://github.com/jenkinsci/slack-plugin/pull/448)
- Custom messages per Build result [PR #445](https://github.com/jenkinsci/slack-plugin/pull/445)

### Bug fixes:

- Don't publish twice, while the build isn't finished yet [PR #446](https://github.com/jenkinsci/slack-plugin/pull/446)
- Fix compatibility for getBotUser [PR #449](https://github.com/jenkinsci/slack-plugin/pull/449)

## 2.9 release notes

### Bug fixes:

- Don't hide custom message if var unset [PR #442](https://github.com/jenkinsci/slack-plugin/pull/442)
- Rename base url to slack compatible app url [PR #443](https://github.com/jenkinsci/slack-plugin/pull/443)

## 2.8 release notes

### Bug fixes:

- Make pipeline response object safer [PR #439](https://github.com/jenkinsci/slack-plugin/pull/439)

## 2.7 release notes

### New features:
- Pipeline response object (threading enhancement) [PR #429](https://github.com/jenkinsci/slack-plugin/pull/429)
- Add message when attachments are present [PR #426](https://github.com/jenkinsci/slack-plugin/pull/426)

## 2.6 release notes

### Bug fixes:
- slack send should not require message [PR #434](https://github.com/jenkinsci/slack-plugin/pull/434)

## 2.5 release notes

### Bug fixes:

- replace # with nothing in chat.postMessage [PR #433](https://github.com/jenkinsci/slack-plugin/pull/433)
- Fix serialisation of some fields [PR #430](https://github.com/jenkinsci/slack-plugin/pull/430)

### Internal updates:

- Re-enable windows tests on CI [PR #424](https://github.com/jenkinsci/slack-plugin/pull/424)

## 2.4 release notes

### New Features:

- Support JCasC [PR #404](https://github.com/jenkinsci/slack-plugin/pull/404) [JENKINS-53641](https://issues.jenkins-ci.org/browse/JENKINS-53641)
  - [example](/README.md#configuration-as-code)
- Add Token Macro support [PR #401](https://github.com/jenkinsci/slack-plugin/pull/401)
- Add slash command support [PR #345](https://github.com/jenkinsci/slack-plugin/pull/345)
- Add thread support [PR #377](https://github.com/jenkinsci/slack-plugin/pull/377)

### Bug Fixes:

- [JENKINS-50706] Fix permission check for credentials checkbox [PR #372](https://github.com/jenkinsci/slack-plugin/pull/372)
- [JENKINS-37339] Prevent NPE after plugin installation [PR #403](https://github.com/jenkinsci/slack-plugin/pull/403)
- [JENKINS-53450] Rename the variable that fails the tests on Windows [PR #394](https://github.com/jenkinsci/slack-plugin/pull/394)
- added @DataBoundSetter for includeTestSummary [PR #393](https://github.com/jenkinsci/slack-plugin/pull/393)
- Reduce verbosity for standard case when the migrator does nothing [PR #395](https://github.com/jenkinsci/slack-plugin/pull/395)
- Create Jenkinsfile [PR #405](https://github.com/jenkinsci/slack-plugin/pull/405)
- slack outgoing webhook isn't working [PR #344](https://github.com/jenkinsci/slack-plugin/pull/344)
- integration credential logging removed [PR #407](https://github.com/jenkinsci/slack-plugin/pull/407)

### Dependency upgrades:

- jackson-databind -> 2.8.11.2, [PR #400](https://github.com/jenkinsci/slack-plugin/pull/400)

### Documentation updates:

- Added multiple channel comment to slackSend [PR #399](https://github.com/jenkinsci/slack-plugin/pull/399)
- Send to users wording tweak [PR #408](https://github.com/jenkinsci/slack-plugin/pull/408)

### Internal updates:

- Parent pom update, new jenkins baseline (2.60.3) and java 8 required  [PR #406](https://github.com/jenkinsci/slack-plugin/pull/406)

## 2.3 release notes

### New Features:

- Send more advanced slack messages v2 [PR #324](https://github.com/jenkinsci/slack-plugin/pull/324)

### Bug Fixes:

- Stop log spam [PR #316](https://github.com/jenkinsci/slack-plugin/pull/316)

## 2.2 release notes

### New Features:

- Implement bot-user support ([#258][#258])
- Use DataBoundSetter for optional params ([#236][#236], [#232][#232])
- Add `Include failed Tests` ([#166][#166])
- Add display failed tests option to slack notifications on job configuration
  ([#275][#275])
- Add baseUrl parameter to support slack-compatible integrations like Mattermost
  or Rocket Chat ([#293][#293])
- Regression notification implemented ([#294][#294])

### Bug Fixes:

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

## 2.1 release notes

### New Features:

- Added Jenkins Credentials support (#247)
- Added support for display-url-api; will allow Blue Ocean to change display
  URLs (#245)
- Added support for @here and other @mentions (#241)
- Reduced log verbosity (#252)
- Updated `@Extension` ordinal value to force earlier migration process (#261)

### UI Improvements:

-  Added credentials selection widget (#247)

### Bug Fixes:

- Fixes for Security issue [JENKINS-35503][JENKINS-35503] (#247)
- Fixed documentation for Pipeline step (#220)

[JENKINS-35503]: https://issues.jenkins-ci.org/browse/JENKINS-35503

## 2.0.1 release notes

Bug Fixes:

- Set webhook endpoint to random uuid based string if not set (PR # 190)
- Upgrading to Slack 2.0 outbound webhooks breaks Jenkins  (#191)

## 2.0 release notes

### New Features:

- Adding configurable webhook endpoint for exposing jenkins commands with a
  Slack outgoing-webhook (#160)
- Added Jenkins Pipeline support via custom step `slackSend`. (#167)

### UI Improvements:

- Job level configuration was moved under post-build actions with support for
  migration from previous versions. (#79, #170)
- Use Subdomain instead of Domain (#168)

### Bug Fixes

- Notify for repeated failures doesn't work (#136)
- Workflow plugin integration (#50)
- "Back to normal" message duration is build duration rather than time the job
  has been broken for (#129)
- [FIXED JENKINS-30559] Dropdown list to select commit info for notifications
  (#133)
- Fix double start message when build is manually started (#96, #137)
- "success" instead of "back to normal" after aborted build (123)
- Doesn't support the Jenkins Inheritance plugin (#6)


## 1.8.1 release notes

This is a backport release which backports features originally intended for
slack-2.0 release.  This is meant as a nice fix for people missing critical
bugs.  Thanks [@Nooba](https://github.com/Nooba/) for taking the time to
backport all of the changes.  Since new features listed here will also be
included in the slack-2.0 release I'm not going to bother mentioning them in the
slack-2.0 release at all.

### New Features:

- Allow simple markup formatting in custom messages. (#127)

### UI Improvements:

- Improves custom message help text. (#97)

### Bug fixes:

- Allow parallel builds. (#122)
- Use textarea for customMessage.  This allows custom messages to have new
  lines. (#103)
- Set test connection message color correctly. (#101)
- Fix publish to multiple rooms. Add unit tests. (#98, #100)
- Include custom message when changes are found. (#95)

## 1.8.0 release notes

### New Features:

- Option to add a custom message (#49, #78)
- Build Server URL defaults to Jenkins URL when the slack plugin is first
  configured (#42, #90)

### UI Improvements:

- Move `Test Connection` button in slack job config to the advanced section
  (#64)
- Improvements to `Test Connection` button.  It now provides feedback in the
  Jenkins UI when it succeeds or fails to connect to the slack instance (#51,
  #81)

### Improvements:

- Improve 'started' message with proper cause (#37)
- The separator for specifying posting to multiple slack channels is more
  robust.  It now allows spaces, commas, and semicolons as a separator (#56)
- Notifications start with full project name when using Cloudbees folders
  plugin.  This includes the folder and the project in the notification (#61)
- Search upstream causes when gathering commit list (#67)
- Change repeated failure message improvement with `Still Failing`.  Makes it
  easier to understand if it is the first or repeated failure in a build (#77)
- Add unit tests for `doTestConnection` method (#82, #84)

### Bug fixes:

- Fix `Include Test Summary` and `Notify Repeated Failure` options being
  reversed when saving settings in the UI (#63)
- Change `.getPreviousBuild()` to allow for higher concurrency (#70)
- Solve `java.lang.NoClassDefFoundError` when running unit tests (#82, #83)
- Adding ability for environment variables and parameters (#31, #80, #89)

## 1.7.0 release notes

### New features:

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

### Improvements:

- Report 'return to normal' on transition from unstable to success (#21).
- Improved logging.

### Bug fixes:

- When changing the global slack settings the slack config in jobs are updated
  as well (#26, #12)
- Fix NullPointerException and output log message if slack is not configured
  (#35, JENKINS-26066)
