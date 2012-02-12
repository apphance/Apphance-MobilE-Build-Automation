package com.apphance.ameba.ios.plugins.fonemonkey

import java.util.Properties;

import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask


class VerifyFoneMonkeySetupTask extends AbstractVerifySetupTask {

    VerifyFoneMonkeySetupTask() {
        super(IOSFoneMonkeyProperty.class)
    }

    @TaskAction
    void verifySetup() {
        def projectProperties = readProperties()
        IOSFoneMonkeyProperty.each {
            if (!it.defaultValue != null) {
                checkProperty(projectProperties, it)
            }
        }
        allPropertiesOK()
    }
}
