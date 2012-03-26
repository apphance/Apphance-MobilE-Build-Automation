package com.apphance.ameba.apphance

import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.apphance.ApphanceProperty;

/**
 * Verifies properties of Apphance integration.
 *
 */
class VerifyApphanceSetupOperation extends AbstractVerifySetupOperation {

    VerifyApphanceSetupOperation() {
        super(ApphanceProperty.class)
    }

    void verifySetup() {
        use (PropertyCategory) {
            def projectProperties = readProperties()
            ApphanceProperty.each {
                checkProperty(projectProperties, it)
            }
            checkIsOnList(ApphanceProperty.APPHANCE_MODE, ['QA', 'Silent'])
            allPropertiesOK()
        }
    }
}
