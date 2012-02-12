package com.apphance.ameba.ios.plugins.framework

import java.util.Properties;

import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask


class VerifyFrameworkSetupTask extends AbstractVerifySetupTask {

    VerifyFrameworkSetupTask() {
        super(IOSFrameworkProperty.class)
    }

    @TaskAction
    void verifySetup() {
        def projectProperties = readProperties()
        IOSFrameworkProperty.each {
            if (!it.defaultValue != null) {
                checkProperty(projectProperties, it)
            }
        }
        allPropertiesOK()
    }
}
