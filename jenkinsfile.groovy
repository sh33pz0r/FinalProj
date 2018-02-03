node("master") {
   withMaven(maven:'m1') {
      stage('Checkout') {
 	 notifySlack()
	 checkout([$class: 'GitSCM', branches: [[name: "ofir"]], doGenerateSubmoduleConfigurations: true, userRemoteConfigs: [[url: "git remote add origin https://github.com/ofirgut007/spring-boot-examples"]]])	
         sh "echo pwd"
      }
      stage('Build') {
         dir('spring-boot-package-war'){
         try {
            sh 'mvn clean package -DskipTests'
	    def pom = readMavenPom file:'pom.xml'
            print pom.version
            env.version = pom.version
	    def workspacePath = pwd()
	    sh "echo ${env.version} > ${workspacePath}/envversion.txt"
	    //mvn versions:set versions:commit -DnewVersion="0.0.25"
	    //mvn scm:checkin -Dincludes=pom.xml -Dmessage="Setting version, preping for release."
	 } catch (exc) {
	    error "ERROR: Failed to package maven"
	 }
	 }
      }
      stage ('test') {
	 dir ('spring-boot-package-war') {
            sh "mvn test"
	    junit 'spring-boot-package-war/target/surefire-reports/TEST-*.xml' 
	 }
      }	
      stage('Deploy') {
         dir ('spring-boot-package-war') {
            def dockerimage = docker.build "spring-boot-package-war:${env.version}"
            //def dockerimage = docker.build "spring-boot-package-war:${env.BUILD_NUMBER}"
            dockerimage.push()
          }
      }
      stage ('Run') {
	 try {
            docker.image("spring-boot-package-war:${env.version}").run('-p 8761:8761')
         } catch (error) {
	 } finally {
	 }
      }

      stage ('Final') {
         //build job: 'do-stuff-with-container-pipeline', wait: false
	 notifySlack(currentBuild.result)
      }
   }
}
      def deploymentOk(){
         def workspacePath = pwd()
         expectedCommitid = new File("${workspacePath}/envversion.txt").text.trim()
         //actualCommitid = readCommitidFromJson()
         println "expected version from txt: ${expectedCommitid}"
         //println "actual version from json: ${actualCommitid}"
         //return expectedCommitid == actualCommitid
	 return 1
      }
      def notifySlack(String buildStatus = 'STARTED') {
	    // Build status of null means success.
	    buildStatus = buildStatus ?: 'SUCCESS'

	    def color

	    if (buildStatus == 'STARTED') {
		color = '#D4DADF'
	    } else if (buildStatus == 'SUCCESS') {
		color = '#BDFFC3'
	    } else if (buildStatus == 'UNSTABLE') {
		color = '#FFFE89'
	    } else {
		color = '#FF9FA1'
	    }

	    def msg = "${buildStatus}: `${env.JOB_NAME}` #${env.BUILD_NUMBER}:\n${env.BUILD_URL}"

	    slackSend(color: color, message: msg)
      }
