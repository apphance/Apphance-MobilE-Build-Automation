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
        logger.lifecycle("Prepares to compile Java for static code analysis")
        File gen = new File(conf.rootDir, 'gen')
        if (!gen.exists() || gen.list().length == 0) {
            logger.lifecycle("Regenerating gen directory by running debug project")
            antExecutor.executeTarget conf.rootDir, DEBUG
        } else {
            logger.lifecycle("Not regenerating gen directory! You might need to run clean in order to get latest data (you can also run any of the android builds)")
        }
    }
}
