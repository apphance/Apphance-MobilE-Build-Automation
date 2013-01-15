package com.apphance.ameba.ios.plugins.kif

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Prepares KIF properties.
 *
 */
class PrepareKIFSetupOperation extends AbstractPrepareSetupOperation {

    Logger logger = Logging.getLogger(PrepareKIFSetupOperation.class)
    ProjectConfiguration conf

    PrepareKIFSetupOperation() {
        super(KifProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
        use(PropertyCategory) {
            IOSXCodeOutputParser iosXcodeOutputParser = new IOSXCodeOutputParser()
            IOSProjectConfiguration iosConf = iosXcodeOutputParser.getIosProjectConfiguration(project)
            KifProperty.each {
                switch (it) {
                    case KifProperty.KIF_CONFIGURATION:
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
