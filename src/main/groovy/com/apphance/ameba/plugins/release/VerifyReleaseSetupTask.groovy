package com.apphance.ameba.plugins.release

import java.util.Properties;

import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupTask


class VerifyReleaseSetupTask extends AbstractVerifySetupTask {

    VerifyReleaseSetupTask() {
        super(ProjectReleaseProperty.class)
    }

    @TaskAction
    void verifySetup() {
        def projectProperties = readProperties()
        ProjectReleaseProperty.each {
            if (!it.defaultValue != null) {
                checkProperty(projectProperties, it)
            }
        }
        allPropertiesOK()
    }
}
