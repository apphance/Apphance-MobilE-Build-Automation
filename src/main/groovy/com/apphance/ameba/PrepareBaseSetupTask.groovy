package com.apphance.ameba

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.TaskAction;

class PrepareBaseSetupTask extends DefaultTask {

    Logger logger = Logging.getLogger(PrepareBaseSetupTask.class)
    ProjectConfiguration conf

    PrepareBaseSetupTask() {
        this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
        this.description = 'Walks you through the base part of setup of the project.'
        this.conf = new ProjectConfiguration()
        //inject myself as dependency for umbrella prepareSetup
        project.prepareSetup.dependsOn(this)
    }

    @TaskAction
    void prepareSetup() {
        // all verification is here
    }
}
