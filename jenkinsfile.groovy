node("master") {
   withMaven(maven:'m1') {
      stage('Checkout') {
         checkout([$class: 'GitSCM', branches: [[name: "ofir"]], doGenerateSubmoduleConfigurations: true, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: "/home/ec2-user/WORKSPACE/"]], submoduleCfg: [], userRemoteConfigs: [[url: "git remote add origin https://github.com/ofirgut007/spring-boot-examples"]]])	
      }
      stage('Build') {
         try {
            sh 'mvn clean test package'
	    def pom = readMavenPom file:'pom.xml'
            print pom.version
            env.version = pom.version
	 } catch (exc) {
	    error "ERROR: Failed to build maven"
	 }
      }
      stage ('test') {
         junit 'spring-boot-package-war/target/surefire-reports/TEST-*.xml' 
      }
      stage('Docker') {
         dir ('discovery-service') {
            def dockerimage = docker.build "localhost:5000/discovery-service:${env.version}"
            dockerimage.push()
            }
         }
      }
      stage ('Run') {
         docker.image("localhost:5000/discovery-service:${env.version}").run('-p 8761:8761 -h discovery --name discovery')
      }
      stage ('Final') {
         build job: 'account-service-pipeline', wait: false
      }      
   }
}
