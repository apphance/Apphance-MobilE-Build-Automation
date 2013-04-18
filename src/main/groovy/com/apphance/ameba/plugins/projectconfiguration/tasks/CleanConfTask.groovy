package com.apphance.ameba.plugins.projectconfiguration.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION

class CleanConfTask extends DefaultTask {

    static String NAME = 'cleanConfiguration'
    String description = 'Cleans configuration before each build'
    String group = AMEBA_CONFIGURATION

    @Inject
    private ProjectConfiguration conf

    @TaskAction
    void clean() {
        conf.buildDir.deleteDir()
        conf.tmpDir.deleteDir()
        conf.logDir.deleteDir()

        conf.buildDir.mkdirs()
        conf.tmpDir.mkdirs()
        conf.logDir.mkdirs()
    }
}
