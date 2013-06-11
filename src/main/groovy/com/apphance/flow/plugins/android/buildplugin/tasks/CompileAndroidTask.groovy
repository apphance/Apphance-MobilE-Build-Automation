package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.executor.AntExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.executor.AntExecutor.DEBUG
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

class CompileAndroidTask extends DefaultTask {

    static String NAME = 'compileAndroid'
    String description = 'Performs code generation/compile tasks for android (if needed)'
    String group = FLOW_BUILD

    @Inject AntExecutor antExecutor
    @Inject AndroidConfiguration conf

    @TaskAction
    void compileAndroid() {
        logger.info("Deleting gen directory")
        new File(conf.rootDir, 'gen').deleteDir()
        antExecutor.executeTarget conf.rootDir, DEBUG
    }
}
