package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

class CleanTask extends DefaultTask {

    static final NAME = 'clean'
    String description = 'Cleans the project'
    String group = AMEBA_BUILD

    @Inject
    CommandExecutor executor
    @Inject
    IOSConfiguration conf

    @TaskAction
    void clean() {
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: ['dot_clean', './']))
        conf.buildDir.deleteDir()
        conf.tmpDir.deleteDir()
        conf.logDir.deleteDir()
        conf.buildDir.mkdirs()
        conf.tmpDir.mkdirs()
        conf.logDir.mkdirs()
    }
}
