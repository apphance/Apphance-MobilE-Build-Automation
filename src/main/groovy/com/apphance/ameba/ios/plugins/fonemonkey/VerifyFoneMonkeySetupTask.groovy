package com.apphance.ameba.ios.plugins.fonemonkey


import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.ios.AbstractVerifyIOSSetupTask
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser


class VerifyFoneMonkeySetupTask extends AbstractVerifyIOSSetupTask {

    VerifyFoneMonkeySetupTask() {
        super(IOSFoneMonkeyProperty.class)
        this.dependsOn(project.verifyIOSSetup)
    }

    @TaskAction
    void verifySetup() {
        def projectProperties = readProperties()
        IOSFoneMonkeyProperty.each {
            if (!it.defaultValue != null) {
                checkProperty(projectProperties, it)
            }
        }
        checkConfiguration(IOSFoneMonkeyProperty.FONE_MONKEY_CONFIGURATION)
        allPropertiesOK()
    }
}
