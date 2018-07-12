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
        stage("build") {
            steps {
                script{
                    def version = readMavenPom().version
                    echo " Building branch = ${BRANCH_NAME} version ${version}"

                    if ( env.BRANCH_NAME ==~ /release/){

                        if ( version ==~ /.*SNAPSHOT/ ){
                            throw new hudson.AbortException('I will not build snapshot-versions on release-branch.')
                        }
                        sh """
                        mvn -B clean
                        mvn -B deploy org.jacoco:jacoco-maven-plugin:prepare-agent                
                    """
                    } else {
                        sh """
                        mvn -B clean
                        mvn -B org.jacoco:jacoco-maven-plugin:prepare-agent verify pmd:pmd javadoc:aggregate                
                    """

                    }
                }

            }
        }

        stage("sonarqube") {
            steps {
                script {
                    if (env.BRANCH_NAME ==~ /master/ ) {
                        echo " Uploading SonarQube results for branch ${BRANCH_NAME}"

                        sh """
                            echo "Uploading results to SonarQube"

                            mvn sonar:sonar \
                                -Dsonar.host.url=http://sonarqube.mcp1.dbc.dk \
                                -Dsonar.login=d8cfb40a9c988e2875590545628605811327660a

                             echo "Done uploading results to SonarQube"

                        """
                    } else {
                        echo " No SonarQube. Branch ${BRANCH_NAME} is not develop"
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
