package com.apphance.ameba.plugins.android.jarlibrary

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Verifies all android jar library properties.
 *
 */
class VerifyAndroidJarLibrarySetupOperation extends AbstractVerifySetupOperation {
    Logger logger = Logging.getLogger(VerifyAndroidJarLibrarySetupOperation.class)

    VerifyAndroidJarLibrarySetupOperation() {
        super(AndroidJarLibraryProperty.class)
    }

    void verifySetup() {
        use(PropertyCategory) {
            def projectProperties = readProperties()
            AndroidJarLibraryProperty.each { checkProperty(projectProperties, it) }
            allPropertiesOK()
        }
    }
}
