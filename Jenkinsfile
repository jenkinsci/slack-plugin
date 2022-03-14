// Builds a module using https://github.com/jenkins-infra/pipeline-library
def configurations = [
        [ platform: "linux", jdk: "8", jenkins: null ],
        [ platform: "linux", jdk: "11", jenkins: null ]
]
buildPlugin(configurations: configurations, useContainerAgent: true)
