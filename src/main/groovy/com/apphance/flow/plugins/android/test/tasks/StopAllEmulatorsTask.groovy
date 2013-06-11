package com.apphance.flow.plugins.android.test.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST

class StopAllEmulatorsTask extends DefaultTask {

    static String NAME = 'stopAllEmulators'
    String group = FLOW_TEST
    String description = 'Stops all emulators and accompanying logcat (includes stopping adb)'

    @Inject AndroidConfiguration conf
    @Inject CommandExecutor executor

    @TaskAction
    void stopAllEmulators() {
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: ['killall', 'emulator-arm'], failOnError: false))
        executor.executeCommand(new Command(runDir: conf.rootDir, cmd: ['killall', 'adb'], failOnError: false))
    }
}
