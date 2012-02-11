package com.apphance.ameba.plugins.projectconfiguration


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction;

import com.apphance.ameba.AmebaCommonBuildTaskGroups;
import com.apphance.ameba.PropertyCategory;

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
        use (PropertyCategory) {
            System.out.print(project.listPropertiesAsString(ProjectBaseProperty.class, true))
        }
    }
}
