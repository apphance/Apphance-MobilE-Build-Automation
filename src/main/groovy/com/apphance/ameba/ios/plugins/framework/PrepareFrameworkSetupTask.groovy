package com.apphance.ameba.ios.plugins.framework

import groovy.io.FileType;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractPrepareSetupTask;
import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser

class PrepareFrameworkSetupTask extends AbstractPrepareSetupTask {

    Logger logger = Logging.getLogger(PrepareFrameworkSetupTask.class)
    ProjectConfiguration conf

    PrepareFrameworkSetupTask() {
        super(IOSFrameworkProperty.class)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        use (PropertyCategory) {
            def headerFiles = getHeaderFiles()
            def resourceFiles = getResourceFiles()
            IOSFrameworkProperty.each {
                IOSXCodeOutputParser iosXcodeOutputParser = new IOSXCodeOutputParser()
                IOSProjectConfiguration iosConf = iosXcodeOutputParser.getIosProjectConfiguration(project)
                switch(it) {
                    case IOSFrameworkProperty.FRAMEWORK_CONFIGURATION:
                        project.getProjectPropertyFromUser(it, iosConf.allconfigurations, br)
                        break
                    case IOSFrameworkProperty.FRAMEWORK_TARGET:
                        project.getProjectPropertyFromUser(it, iosConf.alltargets, br)
                        break
                    case IOSFrameworkProperty.FRAMEWORK_HEADERS:
                        project.getProjectPropertyFromUser(it, headerFiles, br)
                        break
                    case IOSFrameworkProperty.FRAMEWORK_RESOURCES:
                        project.getProjectPropertyFromUser(it, resourceFiles, br)
                        break
                    default:
                        project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendToGeneratedPropertyString(project.listPropertiesAsString(IOSFrameworkProperty.class, false))
        }
    }

    private List getResourceFiles() {
        def resourceFiles = []
        new File('.').eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith(".png")) {
                def path = it.path.startsWith("./") ? it.path.substring(2) : it.path
                resourceFiles << path
            }
        }
        return resourceFiles
    }

    private List getHeaderFiles() {
        def headerFiles = []
        new File('.').eachFileRecurse(FileType.FILES) {
            if (it.name.endsWith(".h")) {
                def path = it.path.startsWith("./") ? it.path.substring(2) : it.path
                headerFiles << path
            }
        }
        return headerFiles
    }
}
