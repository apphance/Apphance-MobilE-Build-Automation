package com.apphance.ameba.ios.plugins.kif

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging

import com.apphance.ameba.AbstractPrepareSetupOperation;
import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser

/**
 * Prepares KIF properties.
 *
 */
class PrepareIosKIFSetupOperation extends AbstractPrepareSetupOperation {

    Logger logger = Logging.getLogger(PrepareIosKIFSetupOperation.class)
    ProjectConfiguration conf

    PrepareIosKIFSetupOperation() {
        super(IOSKifProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
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
            appendProperties()
        }
    }
}
