package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.plugins.android.test.AndroidTestConfiguration
import com.apphance.ameba.executor.AndroidExecutor
import org.gradle.api.Project

import static com.apphance.ameba.plugins.android.test.AndroidTestConfigurationRetriever.getAndroidTestConfiguration
import static org.gradle.api.logging.Logging.getLogger

class CreateAVDTask {

    private l = getLogger(getClass())

    private Project project
    private AndroidTestConfiguration androidTestConf
    private AndroidExecutor androidExecutor

    CreateAVDTask(Project project, AndroidExecutor androidExecutor) {
        this.project = project
        this.androidTestConf = getAndroidTestConfiguration(project)
        this.androidExecutor = androidExecutor
    }

    void createAVD() {
        boolean emulatorExists = androidExecutor.listAvd(project.rootDir).any { it == androidTestConf.emulatorName }
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
