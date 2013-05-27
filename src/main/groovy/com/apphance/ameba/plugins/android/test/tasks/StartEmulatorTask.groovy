package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.configuration.android.AndroidTestConfiguration
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_TEST
import static org.gradle.api.logging.Logging.getLogger

class StartEmulatorTask extends DefaultTask {

    private l = getLogger(getClass())

    static String NAME = 'startEmulator'
    String group = AMEBA_TEST
    String description = 'Starts emulator for manual inspection'

    @Inject CommandExecutor executor
    @Inject AndroidTestConfiguration testConf

    private Process emulatorProcess

    @TaskAction
    void startEmulator() {
        startEmulator(true)
    }

    private void startEmulator(boolean noWindow) {
        l.lifecycle("Starting emulator ${testConf.emulatorName}")
        def emulatorCommand = [
                'emulator',
                '-avd',
                testConf.emulatorName,
                '-port',
                testConf.emulatorPort,
                '-no-boot-anim'
        ]
        if (noWindow) {
            emulatorCommand << '-no-window'
        }
        emulatorProcess = executor.startCommand(new Command(runDir: project.rootDir, cmd: emulatorCommand))
        Thread.sleep(4 * 1000) // sleep for some time.
        runLogCat()
        waitUntilEmulatorReady()
        l.lifecycle("Started emulator ${testConf.emulatorName}")
    }

    private void runLogCat() {
        l.lifecycle("Starting logcat monitor on ${testConf.emulatorName}")
        String[] commandRunLogcat = [
                testConf.getADBBinary(),
                '-s',
                "emulator-${testConf.emulatorPort}",
                'logcat',
                '-v',
                'time'
        ]
        executor.startCommand(new Command(runDir: project.rootDir, cmd: commandRunLogcat))
    }

    private void waitUntilEmulatorReady() {
        l.lifecycle("Waiting until emulator is ready ${testConf.emulatorName}")
        String[] commandRunShell = [
                testConf.getADBBinary(),
                '-s',
                "emulator-${testConf.emulatorPort}",
                'shell',
                'getprop',
                'dev.bootcomplete'
        ]
        def startTime = System.currentTimeMillis()
        while (true) {
            def res = executor.executeCommand(new Command(runDir: project.rootDir, cmd: commandRunShell, failOnError: false))
            if (res != null && res[0] == "1") {
                l.lifecycle("Emulator is ready ${testConf.emulatorName}!")
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
