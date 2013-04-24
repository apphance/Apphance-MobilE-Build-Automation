package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_TEST

class StopAllEmulatorsTask extends DefaultTask {

    static String NAME = 'stopAllEmulators'
    String group = AMEBA_TEST
    String description = 'Stops all emulators and accompanying logcat (includes stopping adb)'

    @Inject
    private CommandExecutor executor

    @TaskAction
    void stopAllEmulators() {
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: ['killall', 'emulator-arm'], failOnError: false))
        executor.executeCommand(new Command(runDir: project.rootDir, cmd: ['killall', 'adb'], failOnError: false))
    }
}
