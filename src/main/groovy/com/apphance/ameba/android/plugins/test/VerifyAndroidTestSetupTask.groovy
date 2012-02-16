package com.apphance.ameba.android.plugins.test


import org.gradle.api.GradleException
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask
import com.apphance.ameba.PropertyCategory


class VerifyAndroidTestSetupTask extends AbstractVerifySetupTask {
    Logger logger = Logging.getLogger(VerifyAndroidTestSetupTask.class)

    VerifyAndroidTestSetupTask() {
        super(AndroidTestProperty.class)
        this.dependsOn(project.verifyAndroidSetup)
    }

    @TaskAction
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
            File dir = new File(project.rootDir,dirName)
            if (!dir.exists()) {
                throw new GradleException("""The directory does not exist ${property.propertyName}: ${dir}""")
            }
            if (!dir.isDirectory()) {
                throw new GradleException("""The file is not directory ${property.propertyName}: ${dir}""")
            }
        }
    }
}
