package com.apphance.ameba.android.plugins.jarlibrary

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory


class VerifyAndroidJarLibrarySetupOperation extends AbstractVerifySetupOperation {
    Logger logger = Logging.getLogger(VerifyAndroidJarLibrarySetupOperation.class)

    VerifyAndroidJarLibrarySetupOperation() {
        super(AndroidJarLibraryProperty.class)
    }

    void verifySetup() {
        use (PropertyCategory) {
            def projectProperties = readProperties()
            AndroidJarLibraryProperty.each{ checkProperty(projectProperties, it) }
            allPropertiesOK()
        }
    }
}
