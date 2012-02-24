package com.apphance.ameba.ios.plugins.framework


import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging

import com.apphance.ameba.AbstractPrepareSetupOperation;
import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser

class PrepareFrameworkSetupOperation extends AbstractPrepareSetupOperation {

    Logger logger = Logging.getLogger(PrepareFrameworkSetupOperation.class)
    ProjectConfiguration conf

    PrepareFrameworkSetupOperation() {
        super(IOSFrameworkProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
        def headerFiles = getFiles { it.name.endsWith('.h')}
        def resourceFiles = getFiles { it.name.endsWith('.png') }
        use (PropertyCategory) {
            IOSXCodeOutputParser iosXcodeOutputParser = new IOSXCodeOutputParser()
            IOSProjectConfiguration iosConf = iosXcodeOutputParser.getIosProjectConfiguration(project)
            IOSFrameworkProperty.each {
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
            appendProperties()
        }
    }
}