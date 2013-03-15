package com.apphance.ameba.plugins.projectconfiguration.tasks

import com.apphance.ameba.AbstractShowSetupOperation
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_SETUP
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME

/**
 * Shows all setup properties for basic configuration plugin.
 *
 */
class ShowSetupTask extends DefaultTask {
    ShowSetupTask() {
        this.description = 'Shows all available project properties'
        this.group = AMEBA_SETUP
        this.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)
    }

    List<AbstractShowSetupOperation> showSetupOperations = []

    @TaskAction
    void showSetup() {
        showSetupOperations.each { it.project = project }
        showSetupOperations.each { it.showSetup() }
    }
}
