package com.apphance.ameba.android.plugins.test.tasks

import com.apphance.ameba.android.plugins.test.AndroidTestConfiguration
import com.apphance.ameba.executor.AndroidExecutor
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
    private AndroidExecutor androidExecutor

    CreateAVDTask(Project project, CommandExecutor executor, AndroidExecutor androidExecutor) {
        this.project = project
        this.androidTestConf = getAndroidTestConfiguration(project)
        this.executor = executor
        this.androidExecutor = androidExecutor
    }

    void createAVD() {
        boolean emulatorExists = androidExecutor.listAvd(project.rootDir).any { it == emulatorName }
        if (!androidTestConf.avdDir.exists() || !emulatorExists) {
            androidTestConf.avdDir.mkdirs()
            l.lifecycle("Creating emulator avd: ${androidTestConf.emulatorName}")
            androidExecutor.createAvdEmulator project.rootDir, androidTestConf.emulatorName, androidTestConf.emulatorTargetName, androidTestConf.emulatorSkin,
                    androidTestConf.emulatorCardSize, androidTestConf.avdDir, androidTestConf.emulatorSnapshotsEnabled
            l.lifecycle("Created emulator avd: ${androidTestConf.emulatorName}")
        } else {
            l.lifecycle("Skipping creating emulator: ${androidTestConf.emulatorName}. It already exists.")
        }
    }
}
