def dockerBuild(dirName){
    script {
        dir(dirName) {

            // Get version from POM
            def version = readMavenPom().version

            // Define docker build Label (image-name:label).
            // For master-branch, label is build-number. all other branches, label is branch-name
            def imageLabel = env.BRANCH_NAME
            if ( ! (imageLabel ==~ /master|trunk/) ) {
                imageLabel = imageLabel.split(/\//)[-1]
                imageLabel = imageLabel.toLowerCase()
            } else {
                imageLabel = env.BUILD_NUMBER
            }
            println("Docker build label is ${imageLabel}")

            // Pickup ArtifactId from POM (to use as image-name)
            modulePom = readMavenPom file: './pom.xml'
            def projectArtifactId = modulePom.getArtifactId()
            if (!projectArtifactId) {
                throw new hudson.AbortException("Unable to find module ArtifactId in ${dirName}/pom.xml remember to add a <ArtifactId> element")
            }
            def imageName = "${projectArtifactId}".toLowerCase()


            // Build docker file
            def app = docker.build("$imageName:${imageLabel}".toLowerCase(), '--pull --no-cache .')

            // Push to Artifactory
            if (currentBuild.resultIsBetterOrEqualTo('SUCCESS')) {
                docker.withRegistry('https://docker-i.dbc.dk', 'docker') {
                    app.push()
                    if (env.BRANCH_NAME ==~ /master|trunk/) {
                        app.push "latest"
                    }
                }
            }
        }
    }
}

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
        stage("build") {
            steps {

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
