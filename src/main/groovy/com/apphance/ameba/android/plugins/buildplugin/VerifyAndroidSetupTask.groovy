package com.apphance.ameba.android.plugins.buildplugin

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser


class VerifyAndroidSetupTask extends AbstractVerifySetupTask {
    Logger logger = Logging.getLogger(VerifyAndroidSetupTask.class)

    IOSXCodeOutputParser iosXCodeOutputParser
    IOSProjectConfiguration iosConf

    VerifyAndroidSetupTask() {
        super(AndroidProjectProperty.class)
    }

    @TaskAction
    void verifySetup() {
        use (PropertyCategory) {
            def projectProperties = readProperties()
            AndroidProjectProperty.each{ checkProperty(projectProperties, it) }
            allPropertiesOK()
        }
    }
}
