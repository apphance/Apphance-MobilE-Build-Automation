package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.plugins.android.test.AndroidTestConfiguration
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.GradleException
import org.gradle.api.Project

import static com.apphance.ameba.plugins.android.test.AndroidTestConfigurationRetriever.getAndroidTestConfiguration
import static org.gradle.api.logging.Logging.getLogger

class StartEmulatorTask {

    private l = getLogger(getClass())

    private Project project
    private CommandExecutor executor
    private AndroidTestConfiguration androidTestConf
    private Process emulatorProcess

    StartEmulatorTask(Project project, CommandExecutor executor) {
        this.project = project
        this.executor = executor
        this.androidTestConf = getAndroidTestConfiguration(project)
    }

    void startEmulator() {
        startEmulator(true)
    }

    private void startEmulator(boolean noWindow) {
        l.lifecycle("Starting emulator ${androidTestConf.emulatorName}")
        androidTestConf.emulatorPort = findFreeEmulatorPort()
        def emulatorCommand = [
                'emulator',
                '-avd',
                androidTestConf.emulatorName,
                '-port',
                androidTestConf.emulatorPort,
                '-no-boot-anim'
        ]
        if (noWindow) {
            emulatorCommand << '-no-window'
        }
        emulatorProcess = executor.startCommand(new Command(runDir: project.rootDir, cmd: emulatorCommand))
        Thread.sleep(4 * 1000) // sleep for some time.
        runLogCat(project)
        waitUntilEmulatorReady()
        l.lifecycle("Started emulator ${androidTestConf.emulatorName}")
    }

    private int findFreeEmulatorPort() {
        int startPort = 5554
        int endPort = 5584
        for (int port = startPort; port <= endPort; port += 2) {
            l.lifecycle("Android emulator probing. trying ports: ${port} ${port + 1}")
            try {
                ServerSocket ss1 = new ServerSocket(port, 0, Inet4Address.getByAddress([127, 0, 0, 1] as byte[]))
                try {
                    ss1.setReuseAddress(true)
                    ServerSocket ss2 = new ServerSocket(port + 1, 0, Inet4Address.getByAddress([127, 0, 0, 1] as byte[]))
                    try {
                        ss2.setReuseAddress(true)
                        l.lifecycle("Success! ${port} ${port + 1} are free")
                        return port
                    } finally {
                        ss2.close()
                    }
                } finally {
                    ss1.close()
                }
            } catch (IOException e) {
                l.lifecycle("Could not obtain ports ${port} ${port + 1}")
            }
        }
        throw new GradleException("Could not find free emulator port (tried all from ${startPort} to ${endPort}!... ")
    }

    private void runLogCat(Project project) {
        l.lifecycle("Starting logcat monitor on ${androidTestConf.emulatorName}")
        String[] commandRunLogcat = [
                androidTestConf.adbBinary,
                '-s',
                "emulator-${androidTestConf.emulatorPort}",
                'logcat',
                '-v',
                'time'
        ]
        executor.startCommand(new Command(runDir: project.rootDir, cmd: commandRunLogcat))
    }

    private void waitUntilEmulatorReady() {
        l.lifecycle("Waiting until emulator is ready ${androidTestConf.emulatorName}")
        String[] commandRunShell = [
                androidTestConf.adbBinary,
                '-s',
                "emulator-${androidTestConf.emulatorPort}",
                'shell',
                'getprop',
                'dev.bootcomplete'
        ]
        def startTime = System.currentTimeMillis()
        while (true) {
            def res = executor.executeCommand(new Command(runDir: project.rootDir, cmd: commandRunShell, failOnError: false))
            if (res != null && res[0] == "1") {
                l.lifecycle("Emulator is ready ${androidTestConf.emulatorName}!")
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
