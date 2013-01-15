package com.apphance.ameba.apphance

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory

/**
 * Verifies properties of Apphance integration.
 *
 */
class VerifyApphanceSetupOperation extends AbstractVerifySetupOperation {

    VerifyApphanceSetupOperation() {
        super(ApphanceProperty.class)
    }

    void verifySetup() {
        use(PropertyCategory) {
            def projectProperties = readProperties()
            ApphanceProperty.each {
                checkProperty(projectProperties, it)
            }
            checkIsOnList(ApphanceProperty.APPHANCE_MODE, ['QA', 'Silent'])
            allPropertiesOK()
        }
    }
}
