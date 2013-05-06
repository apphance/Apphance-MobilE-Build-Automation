package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.Project

class CleanTask {

    private Project project
    private CommandExecutor executor
    private AntBuilder ant

    CleanTask(Project project, CommandExecutor executor) {
        this.project = project
        this.executor = executor
        this.ant = project.ant
    }

    void clean() {
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: ['dot_clean', './']))
        ant.delete(dir: project.file('build'), verbose: true)
        ant.delete(dir: project.file('bin'), verbose: true)
        ant.delete(dir: project.file('tmp'), verbose: true)
    }
}
