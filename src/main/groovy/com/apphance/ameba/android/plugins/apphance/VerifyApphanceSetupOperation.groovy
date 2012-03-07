package com.apphance.ameba.android.plugins.apphance

import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory

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
            checkIsOnList(ApphanceProperty.APPHANCE_MODE, ['QA', 'SILENT'])
            allPropertiesOK()
        }
    }
}
