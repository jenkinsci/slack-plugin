@Library('pipeline-library@warnings-ng-test') _

// Builds a module using https://github.com/jenkins-infra/pipeline-library
def configurations = [
        [ platform: "linux", jdk: "8", jenkins: null ],
        [ platform: "linux", jdk: "11", jenkins: null, javaLevel: "8" ]
]
buildPlugin2(configurations: configurations, useAci: true)
