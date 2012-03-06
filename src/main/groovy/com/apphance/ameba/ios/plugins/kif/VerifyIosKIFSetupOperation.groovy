package com.apphance.ameba.ios.plugins.kif



import com.apphance.ameba.ios.AbstractVerifyIOSSetupOperation
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser;


class VerifyIosKIFSetupOperation extends AbstractVerifyIOSSetupOperation {

    VerifyIosKIFSetupOperation() {
        super(IOSKifProperty.class)
    }

    void verifySetup() {
        super.verifySetup()
        def projectProperties = readProperties()
        IOSKifProperty.each {
            if (!it.defaultValue == null) {
                checkProperty(projectProperties, it)
            }
        }
        checkConfiguration(IOSKifProperty.KIF_CONFIGURATION)
        allPropertiesOK()
    }
}
