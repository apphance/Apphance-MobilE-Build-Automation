package com.apphance.ameba.android.plugins.test.tasks

import com.apphance.ameba.android.plugins.test.AndroidTestConfiguration
import com.apphance.ameba.executor.command.Command
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.Project

import static com.apphance.ameba.android.plugins.test.AndroidTestConfigurationRetriever.getAndroidTestConfiguration
import static org.gradle.api.logging.Logging.getLogger

class CreateAVDTask {

    private l = getLogger(getClass())

    private Project project
    private AndroidTestConfiguration androidTestConf
    private CommandExecutor executor

    CreateAVDTask(Project project, CommandExecutor executor) {
        this.project = project
        this.androidTestConf = getAndroidTestConfiguration(project)
        this.executor = executor
    }

    void createAVD() {
        boolean emulatorExists = executor.executeCommand(new Command(runDir: project.rootDir, cmd:
                [
                        'android',
                        'list',
                        'avd',
                        '-c'
                ])).any { it == androidTestConf.emulatorName }
        if (!androidTestConf.avdDir.exists() || !emulatorExists) {
            androidTestConf.avdDir.mkdirs()
            l.lifecycle("Creating emulator avd: ${androidTestConf.emulatorName}")
            def avdCreateCommand = [
                    'android',
                    '-v',
                    'create',
                    'avd',
                    '-n',
                    androidTestConf.emulatorName,
                    '-t',
                    androidTestConf.emulatorTargetName,
                    '-s',
                    androidTestConf.emulatorSkin,
                    '-c',
                    androidTestConf.emulatorCardSize,
                    '-p',
                    androidTestConf.avdDir,
                    '-f'
            ]
            if (androidTestConf.emulatorSnapshotsEnabled) {
                avdCreateCommand << '-a'
            }
            executor.executeCommand(new Command(runDir: project.rootDir, cmd: avdCreateCommand, failOnError: false, input: ['no']))
            l.lifecycle("Created emulator avd: ${androidTestConf.emulatorName}")
        } else {
            l.lifecycle("Skipping creating emulator: ${androidTestConf.emulatorName}. It already exists.")
        }
    }
}
