package com.apphance.ameba.plugins.projectconfiguration

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import com.apphance.ameba.AbstractShowSetupOperation
import com.apphance.ameba.AmebaCommonBuildTaskGroups

class ShowSetupTask extends DefaultTask {
    ShowSetupTask() {
        this.description = "Shows all available project properties"
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.dependsOn(project.readProjectConfiguration)
    }

    List<AbstractShowSetupOperation> showSetupOperations = []

    @TaskAction
    void showSetup() {
        showSetupOperations.each { it.project = project }
        showSetupOperations.each { it.showSetup() }
    }
}
