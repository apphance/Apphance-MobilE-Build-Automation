package com.apphance.ameba.plugins.projectconfiguration

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction;

import com.apphance.ameba.AmebaCommonBuildTaskGroups;
import com.apphance.ameba.PropertyManager;

class ShowBasePropertiesTask extends DefaultTask {

    ShowBasePropertiesTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = 'Prints all base project properties'
        //inject myself as dependency for umbrella verifySetup
        project.showProperties.dependsOn(this)
        this.dependsOn(project.readProjectConfiguration)
    }


    @TaskAction
    void showProperties() {
        System.out.print(PropertyManager.listPropertiesAsString(project, ProjectBaseProperty.class, true))
    }
}
