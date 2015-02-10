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
