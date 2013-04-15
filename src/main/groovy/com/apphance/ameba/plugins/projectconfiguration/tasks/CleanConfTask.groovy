package com.apphance.ameba.plugins.projectconfiguration.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION

class CleanConfTask extends DefaultTask {

    static String NAME = 'cleanConfiguration'
    String description = 'Cleans configuration before each build'
    String group = AMEBA_CONFIGURATION

    @Inject AndroidConfiguration conf

    @TaskAction
    void clean() {
        conf.buildDir.value.deleteDir()
        conf.tmpDir.value.deleteDir()
        conf.logDir.value.deleteDir()

        conf.buildDir.value.mkdirs()
        conf.tmpDir.value.mkdirs()
        conf.logDir.value.mkdirs()
    }
}
