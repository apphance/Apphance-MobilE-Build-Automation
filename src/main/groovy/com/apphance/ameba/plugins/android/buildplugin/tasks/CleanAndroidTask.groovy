package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.executor.AntExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.executor.AntExecutor.CLEAN
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class CleanAndroidTask extends DefaultTask {

    static String NAME = 'cleanAndroid'
    String description = 'Cleans the application'
    String group = AMEBA_BUILD

    @Inject AndroidConfiguration conf
    @Inject AntExecutor antExecutor

    @TaskAction
    void cleanAndroid() {
        antExecutor.executeTarget project.rootDir, CLEAN
        ant.delete(dir: conf.tmpDir)
    }
}
