tools { 
        maven 'Maven 3.3.9' 
        jdk 'jdk8' 
    }

	properties([
        disableConcurrentBuilds(),
        parameters([
                string(defaultValue: '', description: '', name: 'my_branch'),
        ]),
        pipelineTriggers([])
])

node("master") {
	stage('Check-out') {
            checkout([$class: 'GitSCM', branches: [[name: "${params.my_branch}"]], doGenerateSubmoduleConfigurations: true, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: "/home/ec2-user/WORKSPACE/"]], submoduleCfg: [], userRemoteConfigs: [[url: "git remote add origin https://github.com/ofirgut007/spring-boot-examples"]]])	
	}

    stage('Build') {
        try {
            sh 'mvn -Dmaven.test.failure.ignore=true install' 
	} catch (exc) {
	    error "ERROR: Failed to checkout branch - ${params.my_branch}"
	}
    }
	stage ('test') {
			junit 'target/surefire-reports/**/*.xml' 
	}
    stage('Deploy') {
        build DOCKER

    }
}