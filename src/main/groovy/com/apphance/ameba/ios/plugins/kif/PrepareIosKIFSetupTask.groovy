package com.apphance.ameba.ios.plugins.kif

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction;

import com.apphance.ameba.AbstractPrepareSetupTask;
import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser

class PrepareIosKIFSetupTask extends AbstractPrepareSetupTask {

    Logger logger = Logging.getLogger(PrepareIosKIFSetupTask.class)
    ProjectConfiguration conf

    PrepareIosKIFSetupTask() {
        super(IOSKifProperty.class)
    }

    @TaskAction
    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
        use (PropertyCategory) {
            IOSXCodeOutputParser iosXcodeOutputParser = new IOSXCodeOutputParser()
            IOSProjectConfiguration iosConf = iosXcodeOutputParser.getIosProjectConfiguration(project)
            IOSKifProperty.each {
                switch(it) {
                    case IOSKifProperty.KIF_CONFIGURATION:
                        project.getProjectPropertyFromUser(it, iosConf.allconfigurations, br)
                        break
                    default:
                        project.getProjectPropertyFromUser(it, null, br)
                }
            }
            appendToGeneratedPropertyString(project.listPropertiesAsString(IOSKifProperty.class, false))
        }
    }
}
