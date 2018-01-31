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
            sh "mkdir service"
	} catch (exc) {
	    error "ERROR: Failed to checkout branch - ${params.my_branch}"
	}
    }
	stage ('test') {
	
	}
    stage('Deploy') {
        build DOCKER

    }
}