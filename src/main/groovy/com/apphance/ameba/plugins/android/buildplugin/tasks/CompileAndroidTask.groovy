package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.executor.AntExecutor
import org.gradle.api.Project

import static com.apphance.ameba.executor.AntExecutor.DEBUG
import static org.gradle.api.logging.Logging.getLogger

//TODO refactor/test
class CompileAndroidTask {

    private l = getLogger(getClass())

    private Project project
    private AntExecutor antExecutor

    CompileAndroidTask(Project project, AntExecutor antExecutor) {
        this.project = project
        this.antExecutor = antExecutor
    }

    void compileAndroid() {
        l.lifecycle("Prepares to compile Java for static code analysis")
        File gen = project.file('gen')
        if (!gen.exists() || gen.list().length == 0) {
            l.lifecycle("Regenerating gen directory by running debug project")
            antExecutor.executeTarget DEBUG
        } else {
            l.lifecycle("Not regenerating gen directory! You might need to run clean in order to get latest data (you can also run any of the android builds)")
        }
    }
}
