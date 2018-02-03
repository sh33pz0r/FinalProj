node("master") {
   withMaven(maven:'m1') {
      stage('Checkout') {
         checkout([$class: 'GitSCM', branches: [[name: "ofir"]], doGenerateSubmoduleConfigurations: true, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: "/home/ec2-user/WORKSPACE/"]], submoduleCfg: [], userRemoteConfigs: [[url: "git remote add origin https://github.com/ofirgut007/spring-boot-examples"]]])	
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
	    mvn versions:set versions:commit -DnewVersion="0.0.25"
	    mvn scm:checkin -Dincludes=pom.xml -Dmessage="Setting version, preping for release."
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

   }
}
