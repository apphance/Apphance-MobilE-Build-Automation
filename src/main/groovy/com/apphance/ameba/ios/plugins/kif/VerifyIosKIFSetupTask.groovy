package com.apphance.ameba.ios.plugins.kif


import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask


class VerifyIosKIFSetupTask extends AbstractVerifySetupTask<IOSKifProperty> {
    VerifyIosKIFSetupTask() {
        super(IOSKifProperty.class)
    }

    @TaskAction
    void verifySetup() {
        def projectProperties = readProperties()
        IOSKifProperty.each {
            if (!it.optional) {
                checkProperty(projectProperties, it)
            }
        }
        allPropertiesOK()
    }
}
