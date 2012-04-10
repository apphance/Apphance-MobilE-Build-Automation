package com.apphance.ameba.android.plugins.test


import org.gradle.api.GradleException
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory


/**
 * Verifies if android properties are setup correctly.
 *
 */
class VerifyAndroidTestSetupOperation extends AbstractVerifySetupOperation {
    Logger logger = Logging.getLogger(VerifyAndroidTestSetupOperation.class)

    VerifyAndroidTestSetupOperation() {
        super(AndroidTestProperty.class)
    }

    void verifySetup() {
        use (PropertyCategory) {
            def projectProperties = readProperties()
            AndroidTestProperty.each{ checkProperty(projectProperties, it) }
            checkBoolean(AndroidTestProperty.EMULATOR_NO_WINDOW)
            checkBoolean(AndroidTestProperty.EMULATOR_SNAPSHOT_ENABLED)
            checkBoolean(AndroidTestProperty.TEST_PER_PACKAGE)
            checkBoolean(AndroidTestProperty.USE_EMMA)
            checkDirectory(AndroidTestProperty.TEST_DIRECTORY)
            allPropertiesOK()
        }
    }

    void checkDirectory(property) {
        use (PropertyCategory) {
            def dirName = project.readProperty(property)
            File dir = project.file(dirName)
            if (!dir.exists()) {
                throw new GradleException("""The directory does not exist ${property.propertyName}: ${dir}""")
            }
            if (!dir.isDirectory()) {
                throw new GradleException("""The file is not directory ${property.propertyName}: ${dir}""")
            }
        }
    }
}
