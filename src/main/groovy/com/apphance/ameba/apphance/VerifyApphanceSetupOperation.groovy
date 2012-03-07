package com.apphance.ameba.apphance

import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.plugins.apphance.ApphanceProperty;

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
            allPropertiesOK()
        }
    }
}
