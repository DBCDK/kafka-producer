def branch
def version

pipeline {
    agent {
        label "devel8"
    }
    tools {
        maven "maven 3.5"
    }
    environment {
        MAVEN_OPTS = "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
    }
    triggers {
        pollSCM("H/3 * * * *")
    }
    options {
        buildDiscarder(logRotator(artifactDaysToKeepStr: "", artifactNumToKeepStr: "", daysToKeepStr: "30", numToKeepStr: "30"))
        timestamps()
        disableConcurrentBuilds()
    }


    stages {
        stage("set environment"){
            steps{
                script {
                    if (! env.BRANCH_NAME) {
                        currentBuild.rawBuild.result = Result.ABORTED
                        throw new hudson.AbortException('Job Started from non MultiBranch Build')
                    } else {
                        echo " Building BRANCH_NAME == ${BRANCH_NAME}"
                    }
                    version = readMavenPom().version
                    if (!env.CHANGE_BRANCH) {
                        branch = env.BRANCH_NAME
                    } else {
                        branch = env.CHANGE_BRANCH
                    }
                }
            }
        }
        stage("build") {
            steps {
                script{
                    if ( branch ==~ /master/){
                        if (version ==~/SNAPSHOT/){
                            currentBuild.rawBuild.result = Result.ABORTED
                            throw new hudson.AbortException('I will not build snapshot-versions on master-branch.')
                        }
                        sh """
                        mvn -B clean
                        mvn -B deploy org.jacoco:jacoco-maven-plugin:prepare-agent                
                    """
                    } else {
                        sh """
                        mvn -B clean
                        mvn -B verify org.jacoco:jacoco-maven-plugin:prepare-agent                
                    """

                    }
                }

            }
        }

        stage("sonarqube") {
            steps {
                script {
                    if (branch ==~ /develop/) {
                        echo " Uploading SonarQube results for branch ${BRANCH_NAME}"

                        sh """
                            echo "Uploading results to SonarQube"

                            mvn sonar:sonar \
                                -Dsonar.host.url=http://sonarqube.mcp1.dbc.dk \
                                -Dsonar.login=d8cfb40a9c988e2875590545628605811327660a

                             echo "Done uploading results to SonarQube"

                        """
                    } else {
                        echo " No SonarQube. Branch ${BRANCH_NAME} is not master"
                    }
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
