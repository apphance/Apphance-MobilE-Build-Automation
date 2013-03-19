package com.apphance.ameba.android.plugins.test.tasks

import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.Project

class StopAllEmulatorsTask {

    private Project project
    private CommandExecutor executor

    StopAllEmulatorsTask(Project project, CommandExecutor executor) {
        this.project = project
        this.executor = executor
    }

    void stopAllEmulators() {
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: ['killall', 'emulator-arm'], failOnError: false))
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: ['killall', 'adb'], failOnError: false))
    }
}
