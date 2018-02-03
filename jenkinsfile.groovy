pipeline {
  environment {
    M3_HOME = "/usr/share/maven"
    M2_HOME = "/usr/share/maven"
  }
  agent none
  tools {
      maven 'Maven 3.3.9'
      jdk 'jdk8'
  }
  stages {
    // stage('Send Slack Notifcation') {
    //     steps {
    //         slackSend (color: '#FFFF00', message: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
    //     }
    // }
    stage("docker") {
        agent docker: "maven:3.3.9-jdk-8"

            steps {
                checkout scm
                sh "mvn clean ${env.MAVEN_GOAL} -B -Dmaven.test.failure.ignore=true"
            }

            post {
                success {
                    archive "**/target/**/*.jar"
                    junit '**/target/surefire-reports/*.xml'
                }
            }
    }
    stage('verify') {
      steps {
          dir("spring-boot-package-war"){
                sh 'mvn clean verify'
          }
      }
    }
    stage ('Build') {
        steps {
            sh 'mvn install'
        }
        post {
            success {
                junit 'target/surefire-reports/**/*.xml'
            }
        }
    }
  }
//   post {
//     success {
//       slackSend (color: '#00FF00', message: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
//     }

//     failure {
//       slackSend (color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
//     }
//   }
}
