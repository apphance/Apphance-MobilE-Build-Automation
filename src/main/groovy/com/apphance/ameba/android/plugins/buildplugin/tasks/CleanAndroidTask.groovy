package com.apphance.ameba.android.plugins.buildplugin.tasks

import com.apphance.ameba.executor.AntExecutor
import org.gradle.api.Project

import static com.apphance.ameba.executor.AntExecutor.CLEAN

//TODO refactor/test
class CleanAndroidTask {

    private Project project
    private AntExecutor antExecutor

    CleanAndroidTask(Project project, AntExecutor antExecutor) {
        this.project = project
        this.antExecutor = antExecutor
    }

    void cleanAndroid() {
        antExecutor.executeTarget CLEAN
        File tmpDir = project.file("tmp")
        project.ant.delete(dir: tmpDir)
    }
}
