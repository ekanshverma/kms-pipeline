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

