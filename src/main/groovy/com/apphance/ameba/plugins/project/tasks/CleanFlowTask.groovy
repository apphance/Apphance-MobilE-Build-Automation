package com.apphance.ameba.plugins.project.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.FlowTasksGroups.FLOW_SETUP

class CleanFlowTask extends DefaultTask {

    static String NAME = 'cleanFlow'
    String description = 'Cleans flow temporary folders before each build'
    String group = FLOW_SETUP

    @Inject ProjectConfiguration conf

    @TaskAction
    void clean() {
        conf.buildDir.deleteDir()
        conf.buildDir.mkdirs()

        conf.tmpDir.deleteDir()
        conf.tmpDir.mkdirs()

        conf.logDir.deleteDir()
        conf.logDir.mkdirs()
    }
}
