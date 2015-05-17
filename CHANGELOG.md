# 1.8.0 release notes

New Features:

* Option to add a custom message (#49, #78)
* Build Server URL defaults to Jenkins URL when the slack plugin is first
  configured (#42, #90)

UI Improvements:

* Move `Test Connection` button in slack job config to the advanced section
  (#64)
* Improvements to `Test Connection` button.  It now provides feedback in the
  Jenkins UI when it succeeds or fails to connect to the slack instance (#51,
  #81)

Improvements:

* Improve 'started' message with proper cause (#37)
* The separator for specifying posting to multiple slack channels is more
  robust.  It now allows spaces, commas, and semicolons as a separator (#56)
* Notifications start with full project name when using Cloudbees folders
  plugin.  This includes the folder and the project in the notification (#61)
* Search upstream causes when gathering commit list (#67)
* Change repeated failure message improvement with `Still Failing`.  Makes it
  easier to understand if it is the first or repeated failure in a build (#77)
* Add unit tests for `doTestConnection` method (#82, #84)

Bug fixes:

* Fix `Include Test Summary` and `Notify Repeated Failure` options being
  reversed when saving settings in the UI (#63)
* Change `.getPreviousBuild()` to allow for higher concurrency (#70)
* Solve `java.lang.NoClassDefFoundError` when running unit tests (#82, #83)
* Adding ability for environment variables and parameters (#31, #80, #89)

# 1.7.0 release notes

New features:

* Advanced settings
  * Team domain and token can be specified per project; falls back to global
    config if not specified (#19)
  * Post a list of commits with the build notification (title and author of
    commit) (#30, #44, #45)
  * Include JUnit test summary in build notifications (#25)
* Use colors when sending a message using slack (#20, #24).  Also, the start
  notifications use the color of the last build result (#43)
* Support for authenticated proxies (#14)
* Test Connection button (#46, #28)
* Option to disable subsequent notifications on build failures (#46, #15)

Improvements:

* Report 'return to normal' on transition from unstable to success (#21).
* Improved logging.

Bug fixes:

* When changing the global slack settings the slack config in jobs are updated
  as well (#26, #12)
* Fix NullPointerException and output log message if slack is not configured
  (#35, JENKINS-26066)
