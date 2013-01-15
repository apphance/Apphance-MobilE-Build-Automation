package com.apphance.ameba.plugins.projectconfiguration

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.AmebaCommonBuildTaskGroups
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Verifies all properties.
 *
 */
class VerifySetupTask extends DefaultTask {
    VerifySetupTask() {
        this.description = "Verifies if the project can be build properly"
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.dependsOn(project.readProjectConfiguration)
    }

    List<AbstractVerifySetupOperation> verifySetupOperations = []

    @TaskAction
    void verifySetup() {
        verifySetupOperations.each { it.project = project }
        verifySetupOperations.each { it.verifySetup() }
    }
}
