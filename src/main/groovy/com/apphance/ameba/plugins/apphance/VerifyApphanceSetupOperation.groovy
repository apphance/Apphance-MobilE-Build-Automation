package com.apphance.ameba.plugins.apphance

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory

import static com.apphance.ameba.plugins.apphance.ApphanceProperty.APPHANCE_LIB
import static com.apphance.ameba.plugins.apphance.ApphanceProperty.APPHANCE_MODE

/**
 * Verifies properties of Apphance integration.
 *
 */
class VerifyApphanceSetupOperation extends AbstractVerifySetupOperation {

    def ALLOWED_EMPTY_PROPERTIES = [APPHANCE_LIB]

    VerifyApphanceSetupOperation() {
        super(ApphanceProperty.class)
    }

    void verifySetup() {
        use(PropertyCategory) {
            def projectProperties = readProperties()
            ApphanceProperty.each {
                checkProperty(projectProperties, it, ALLOWED_EMPTY_PROPERTIES)
            }
            checkIsOnList(APPHANCE_MODE, ['QA', 'Silent'])
            allPropertiesOK()
        }
    }
}
