package com.apphance.flow.plugins.android.test.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidTestConfiguration
import com.apphance.flow.executor.ExecutableCommand
import com.apphance.flow.executor.command.Command
import com.apphance.flow.executor.command.CommandExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import javax.inject.Named

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST

class StartEmulatorTask extends DefaultTask {

    static String NAME = 'startEmulator'
    String group = FLOW_TEST
    String description = 'Starts emulator for manual inspection'

    @Inject CommandExecutor executor
    @Inject AndroidConfiguration conf
    @Inject AndroidTestConfiguration testConf
    @Inject
    @Named('executable.emulator') ExecutableCommand executableEmulator
    @Inject
    @Named('executable.adb') ExecutableCommand executableAdb

    private Process emulatorProcess

    @TaskAction
    void startEmulator() {
        startEmulator(true)
    }

    private void startEmulator(boolean noWindow) {
        logger.lifecycle("Starting emulator ${testConf.emulatorName}")
        def emulatorCommand = executableEmulator.cmd + [
                '-avd',
                testConf.emulatorName,
                '-port',
                testConf.emulatorPort,
                '-no-boot-anim'
        ]
        if (noWindow) {
            emulatorCommand << '-no-window'
        }
        emulatorProcess = executor.startCommand(new Command(runDir: conf.rootDir, cmd: emulatorCommand))
        Thread.sleep(4 * 1000) // sleep for some time.
        runLogCat()
        waitUntilEmulatorReady()
        logger.lifecycle("Started emulator ${testConf.emulatorName}")
    }

    private void runLogCat() {
        logger.lifecycle("Starting logcat monitor on ${testConf.emulatorName}")
        String[] commandRunLogcat = executableAdb.cmd + [
                '-s',
                "emulator-${testConf.emulatorPort}",
                'logcat',
                '-v',
                'time'
        ]
        executor.startCommand(new Command(runDir: conf.rootDir, cmd: commandRunLogcat))
    }

    private void waitUntilEmulatorReady() {
        logger.lifecycle("Waiting until emulator is ready ${testConf.emulatorName}")
        String[] commandRunShell = executableAdb.cmd + [
                '-s',
                "emulator-${testConf.emulatorPort}",
                'shell',
                'getprop',
                'dev.bootcomplete'
        ]
        def startTime = System.currentTimeMillis()
        while (true) {
            def res = executor.executeCommand(new Command(runDir: conf.rootDir, cmd: commandRunShell, failOnError: false))
            if (res != null && res[0] == "1") {
                logger.lifecycle("Emulator is ready ${testConf.emulatorName}!")
                break
            }
            if (System.currentTimeMillis() - startTime > 360 * 1000) {
                emulatorProcess?.destroy()
                throw new GradleException("Could not start emulator in  ${360 * 1000 / 1000.0} s. Giving up.")
            }
            Thread.sleep(4 * 1000)
        }
    }
}
