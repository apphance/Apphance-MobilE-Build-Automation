package com.apphance.ameba.ios.plugins.kif


import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.ios.AbstractVerifyIOSSetupTask
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser;


class VerifyIosKIFSetupTask extends AbstractVerifyIOSSetupTask {

    VerifyIosKIFSetupTask() {
        super(IOSKifProperty.class)
        this.dependsOn(project.verifyBaseSetup)
    }

    @TaskAction
    void verifySetup() {
        def projectProperties = readProperties()
        IOSKifProperty.each {
            if (!it.defaultValue == null) {
                checkProperty(projectProperties, it)
            }
        }
        checkConfiguration(IOSKifProperty.KIF_CONFIGURATION)
        allPropertiesOK()
    }
}
