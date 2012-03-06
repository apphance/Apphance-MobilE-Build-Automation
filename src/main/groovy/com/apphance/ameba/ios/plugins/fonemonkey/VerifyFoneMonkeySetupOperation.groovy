package com.apphance.ameba.ios.plugins.fonemonkey



import com.apphance.ameba.ios.AbstractVerifyIOSSetupOperation
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser


class VerifyFoneMonkeySetupOperation extends AbstractVerifyIOSSetupOperation {

    VerifyFoneMonkeySetupOperation() {
        super(IOSFoneMonkeyProperty.class)
    }

    void verifySetup() {
        super.verifySetup()
        def projectProperties = readProperties()
        IOSFoneMonkeyProperty.each {
            if (!it.defaultValue != null) {
                checkProperty(projectProperties, it)
            }
        }
        checkConfiguration(IOSFoneMonkeyProperty.FONE_MONKEY_CONFIGURATION)
        allPropertiesOK()
    }
}
