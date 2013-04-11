package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.executor.AntExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.executor.AntExecutor.CLEAN
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class CleanAndroidTask extends DefaultTask {

    static String NAME = 'cleanAndroid'
    String description = 'Cleans the application'
    String group = AMEBA_BUILD

    @TaskAction
    void cleanAndroid() {
        new AntExecutor(project.rootDir).executeTarget CLEAN
        File tmpDir = project.file("tmp")
        project.ant.delete(dir: tmpDir)
    }
}
