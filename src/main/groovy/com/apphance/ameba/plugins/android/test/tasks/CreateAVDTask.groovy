package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidTestConfiguration
import com.apphance.ameba.executor.AndroidExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_TEST
import static org.gradle.api.logging.Logging.getLogger

class CreateAVDTask extends DefaultTask {

    static String NAME = 'createAVD'
    String group = AMEBA_TEST
    String description = 'Prepares AVDs for emulator'

    private l = getLogger(getClass())

    @Inject
    AndroidConfiguration conf
    @Inject
    AndroidTestConfiguration testConf
    @Inject
    AndroidExecutor androidExecutor

    @TaskAction
    void createAVD() {
        boolean emulatorExists = androidExecutor.listAvd().any { it == testConf.emulatorName }
        if (!testConf.getAVDDir().exists() || !emulatorExists) {
            testConf.getAVDDir().mkdirs()
            l.lifecycle("Creating emulator avd: ${testConf.emulatorName}")
            androidExecutor.createAvdEmulator(
                    conf.rootDir,
                    testConf.emulatorName,
                    testConf.emulatorTarget.value,
                    testConf.emulatorSkin.value,
                    testConf.emulatorCardSize.value,
                    testConf.getAVDDir(),
                    testConf.emulatorSnapshotEnabled.value)
            l.lifecycle("Created emulator avd: ${testConf.emulatorName}")
        } else {
            l.lifecycle("Skipping creating emulator: ${testConf.emulatorName}. It already exists.")
        }
    }
}
