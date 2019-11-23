import groovy.json.JsonBuilder  
import groovy.json.JsonSlurper  
def cleanWorkspace()
    {
        echo "Cleaning up ${WORKSPACE}"
        // clean up our workspace 
        deleteDir()
        // clean up tmp directory 
        dir("${workspace}@tmp") {
            deleteDir()            
        }
    }
def getECRName(){
    String repo = steps.sh(returnStdout: true, script: "aws ecr get-login --region us-east-1 --no-include-email | cut -d \" \" -f 7" ).trim()
    return repo.replace("https://", "")
}
def getOrgName(){
        //return "${scm.getUserRemoteConfigs()}".toString().split(' ')[2].split('/')[3]
        return "${scm.getUserRemoteConfigs()[0].getUrl()}".toString().split('/')[3]
}
def getRepoURL(){
        //return "${scm.getUserRemoteConfigs()}".toString().split(' ')[2].split('/')[2]
        return "${scm.getUserRemoteConfigs()[0].getUrl()}".toString()
}
def getAppName() {
        return "${scm.getUserRemoteConfigs()[0].getUrl()}".toString().split('/').last().split('\\.').first()
    }
def getEnvironment(branchName)
    {   
        branchName = branchName.toLowerCase()
        if (branchName == 'master')
            return 'prod'
        else if (branchName == 'dev')
            return 'dev'
        else if (branchName == 'qa')
            return 'qa'
        else if (branchName == 'release')
            return 'qa'
        else
            return branchName
    }
def createDockerRegistry(def dockerRepo) {
    def sout = new StringBuilder(), serr = new StringBuilder()
    command = "aws ecr describe-repositories --region us-east-1"
    def proc = command.execute()
    proc.consumeProcessOutput(sout, serr)
    proc.waitForOrKill(10000)
    if ("$serr"){
        return "Unable to find registry information error is ${serr}"
    }
    else{
        if("$sout"){
            def str = "${sout}"
            def parser = new JsonSlurper()
            def json = parser.parseText(str)
            def reg_found = false
            assert json instanceof Map
            json.repositories.repositoryName.each{ 
                v -> if(v.toString().contains(dockerRepo)){
                    reg_found = true
                }
            }
            if(reg_found == true){
                println("Registry exists")
            }
            else{
                println("Creating registry now ...")
                def sout_rc = new StringBuilder(), serr_rc = new StringBuilder()
                command = "aws ecr create-repository --repository-name ${dockerRepo} --region us-east-1"
                def proc_rc = command.execute()
                proc_rc.consumeProcessOutput(sout_rc, serr_rc)
                proc.waitForOrKill(10000)
                if(serr){
                    println("Error creating repository")
                    return
                }
            }
        }
        else{
            println("Creating registry now ...")
            def sout_rc = new StringBuilder(), serr_rc = new StringBuilder()
            command = "aws ecr create-repository --repository-name ${dockerRepo} --region us-east-1"
            def proc_rc = command.execute()
            proc_rc.consumeProcessOutput(sout_rc, serr_rc)
            proc.waitForOrKill(10000)
            if(serr){
                println("Error creating repository")
                return "${sout_rc} : ${$err_rc}"
            }
            return "${sout_rc} : ${serr_rc}"
        }
    }
}
def createDockerfile(){
    if (fileExists('pom.xml')){
        def dockerfile = libraryResource 'Dockerfile.gradle'
        println('Identified as Gradle prject...')
        if (fileExists('Dockerfile')){
            println('\tDockerfile exists not creating...')
        }
        else{
            writeFile file: 'Dockerfile', text: dockerfile
        }
    }
    else{
        println('Unable to identify build type, not creating dockerfile...')
    }
}

def createJenkinsfile(){
    if (fileExists('pom.xml')){
        if (fileExists('Jenkinsfile')){
            println('\tJenkinsfile exists not creating...')
        }
        else{
            println('Creating Jenkinsfile for gradle project...')
            writeFile file: 'Jenkinsfile', text: '@Library(\'kms-pipeline\') _\nmvnBuild(deploy: true)'
        }
    }
    else{
        println('Unable to identify build type, not creating Jenkinsfile...')
    }
}

