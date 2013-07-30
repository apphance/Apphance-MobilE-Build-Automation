package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

class UpdateProjectTask extends DefaultTask {

    static String NAME = 'updateProject'
    String description = 'Updates project using android command line tool'
    String group = FLOW_BUILD

    @Inject AndroidConfiguration conf
    @Inject AndroidProjectUpdater projectUpdater

    @TaskAction
    void runUpdate() {
        projectUpdater.updateRecursively conf.rootDir, conf.target.value, conf.projectName.value
    }
}
