package com.apphance.ameba.plugins.projectconfiguration.tasks

import com.apphance.ameba.AbstractVerifySetupOperation
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_SETUP
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME

/**
 * Verifies all properties.
 *
 */
class VerifySetupTask extends DefaultTask {

    VerifySetupTask() {
        this.description = 'Verifies if the project can be build properly'
        this.group = AMEBA_SETUP
        this.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)
    }

    List<AbstractVerifySetupOperation> verifySetupOperations = []

    @TaskAction
    void verifySetup() {
        verifySetupOperations.each { it.project = project }
        verifySetupOperations.each { it.verifySetup() }
    }
}
