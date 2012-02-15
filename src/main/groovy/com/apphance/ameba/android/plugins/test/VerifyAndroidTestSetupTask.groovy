package com.apphance.ameba.android.plugins.test

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
            allPropertiesOK()
        }
    }
}
