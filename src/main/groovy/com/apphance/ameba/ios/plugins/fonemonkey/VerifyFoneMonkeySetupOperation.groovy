package com.apphance.ameba.ios.plugins.fonemonkey

import com.apphance.ameba.ios.AbstractVerifyIOSSetupOperation
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser


/**
 * Verifies if all FoneMonkey properties are setup properly.
 *
 */
class VerifyFoneMonkeySetupOperation extends AbstractVerifyIOSSetupOperation {

    VerifyFoneMonkeySetupOperation() {
        super(FoneMonkeyProperty.class)
    }

    void verifySetup() {
        super.verifySetup()
        def projectProperties = readProperties()
        FoneMonkeyProperty.each {
            if (!it.defaultValue != null) {
                checkProperty(projectProperties, it)
            }
        }
        checkConfiguration(FoneMonkeyProperty.FONE_MONKEY_CONFIGURATION)
        allPropertiesOK()
    }
}
