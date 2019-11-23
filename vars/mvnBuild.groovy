def call(Map pipelineArgs){
  
pipeline {
  agent any
  options {
    timeout(time: 2, unit: 'HOURS') 
    disableConcurrentBuilds()
  }
  environment {
    ORG = utility.getOrgName()
    APP_NAME = utility.getAppName()
    //CHARTMUSEUM_CREDS = credentials('jenkins-x-chartmuseum')
    DOCKER_REGISTRY_ORG = utility.getECRName()
    ENV_NAME = utility.getEnvironment("$BRANCH_NAME")
    BUILD_OPTS = 'clean build -x test' //'clean build jacocoTestReport' //
    //BUILD_OPTS = 'clean build'
    GIT_CRED_ID = 'ekansh-github'
    REPO_URL = utility.getRepoURL()
  }
  stages {
       stage('Git Pull') {
      when { anyOf { branch 'dev'; branch 'release'} }
      environment {
        //PREVIEW_VERSION = "$BRANCH_NAME-$BUILD_NUMBER".replaceAll('\\/','_')
        PREVIEW_VERSION = "$BRANCH_NAME-$BUILD_NUMBER"
        PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
        HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
        DOCKER_REGISTRY = "${env.DOCKER_REGISTRY_ORG}"
       // DC = utility.createImageSecret("$PREVIEW_NAMESPACE")
      }
      steps {
        // ensure we're not on a detached head
       // sendNotifications 'STARTED'
        sh "git checkout $BRANCH_NAME"
        sh "git config --global credential.helper store"
        sh "git tag -l"
        sh "echo \$(jx-release-version)-$PREVIEW_VERSION > VERSION"
        sh "echo hi poor kid.."
      //  sh "sed -i -e \"s/appVersion.*/appVersion = \'\$(cat VERSION)\'/\" gradle.properties"
      }
    }

  }
}
}
