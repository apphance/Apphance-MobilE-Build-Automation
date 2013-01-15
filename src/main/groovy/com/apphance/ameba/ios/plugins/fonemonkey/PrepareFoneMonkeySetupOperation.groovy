package com.apphance.ameba.ios.plugins.fonemonkey

import com.apphance.ameba.AbstractPrepareSetupOperation
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration
import com.apphance.ameba.ios.IOSXCodeOutputParser
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Prepares properties for FoneMonkey.
 *
 */
class PrepareFoneMonkeySetupOperation extends AbstractPrepareSetupOperation {

    Logger logger = Logging.getLogger(PrepareFoneMonkeySetupOperation.class)

    PrepareFoneMonkeySetupOperation() {
        super(FoneMonkeyProperty.class)
    }

    void prepareSetup() {
        logger.lifecycle("Preparing ${propertyDescription}")
        BufferedReader br = getReader()
        use(PropertyCategory) {
            IOSXCodeOutputParser iosXcodeOutputParser = new IOSXCodeOutputParser()
            IOSProjectConfiguration iosConf = iosXcodeOutputParser.getIosProjectConfiguration(project)
            FoneMonkeyProperty.each {
                switch (it) {
                    case FoneMonkeyProperty.FONE_MONKEY_CONFIGURATION:
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
