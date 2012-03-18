package com.apphance.ameba.android.plugins.apphance

import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory

class VerifyApphanceSetupOperation extends AbstractVerifySetupOperation {

    VerifyApphanceSetupOperation() {
        super(AndroidApphanceProperty.class)
    }

    void verifySetup() {
        use (PropertyCategory) {
            def projectProperties = readProperties()
            AndroidApphanceProperty.each {
                checkProperty(projectProperties, it)
            }
            checkIsOnList(AndroidApphanceProperty.APPHANCE_MODE, ['QA', 'Silent'])
            allPropertiesOK()
        }
    }
}
