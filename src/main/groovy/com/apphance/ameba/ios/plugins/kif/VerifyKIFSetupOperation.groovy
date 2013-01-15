package com.apphance.ameba.ios.plugins.kif

import com.apphance.ameba.ios.AbstractVerifyIOSSetupOperation

/**
 * Verifies KIF-related properties.
 *
 */
class VerifyKIFSetupOperation extends AbstractVerifyIOSSetupOperation {

    VerifyKIFSetupOperation() {
        super(KifProperty.class)
    }

    void verifySetup() {
        super.verifySetup()
        def projectProperties = readProperties()
        KifProperty.each {
            if (!it.defaultValue == null) {
                checkProperty(projectProperties, it)
            }
        }
        checkConfiguration(KifProperty.KIF_CONFIGURATION)
        allPropertiesOK()
    }
}
