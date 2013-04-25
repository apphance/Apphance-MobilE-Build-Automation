package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.configuration.android.AndroidTestConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_TEST

class CleanAVDTask extends DefaultTask {

    static String NAME = 'cleanAVD'
    String group = AMEBA_TEST
    String description = 'Cleans AVDs for emulators'

    @Inject
    AndroidTestConfiguration testConf

    @TaskAction
    void cleanAVD() {
        ant.delete(dir: testConf.getAVDDir())
    }
}
