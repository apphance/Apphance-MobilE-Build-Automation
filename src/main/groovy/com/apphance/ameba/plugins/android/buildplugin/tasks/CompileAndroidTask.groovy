package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.executor.AntExecutor
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.executor.AntExecutor.DEBUG
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static org.gradle.api.logging.Logging.getLogger

class CompileAndroidTask extends DefaultTask{

    @Inject
    AntExecutor antExecutor

    static String NAME = 'compileAndroid'
    String description = 'Performs code generation/compile tasks for android (if needed)'
    String group = AMEBA_BUILD

    private l = getLogger(getClass())

    @TaskAction
    void compileAndroid() {
        l.lifecycle("Prepares to compile Java for static code analysis")
        File gen = project.file('gen')
        if (!gen.exists() || gen.list().length == 0) {
            l.lifecycle("Regenerating gen directory by running debug project")
            antExecutor.executeTarget project.rootDir, DEBUG
        } else {
            l.lifecycle("Not regenerating gen directory! You might need to run clean in order to get latest data (you can also run any of the android builds)")
        }
    }
}
