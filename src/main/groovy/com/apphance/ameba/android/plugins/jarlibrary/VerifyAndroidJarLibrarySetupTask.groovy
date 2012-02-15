package com.apphance.ameba.android.plugins.jarlibrary

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask
import com.apphance.ameba.PropertyCategory


class VerifyAndroidJarLibrarySetupTask extends AbstractVerifySetupTask {
    Logger logger = Logging.getLogger(VerifyAndroidJarLibrarySetupTask.class)

    VerifyAndroidJarLibrarySetupTask() {
        super(AndroidJarLibraryProperty.class)
    }

    @TaskAction
    void verifySetup() {
        use (PropertyCategory) {
            def projectProperties = readProperties()
            PrepareAndroidJarLibrarySetupTask.each{ checkProperty(projectProperties, it) }
            allPropertiesOK()
        }
    }
}
