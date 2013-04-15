package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.configuration.android.AndroidTestConfiguration
import org.gradle.api.DefaultTask

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_TEST

class CleanAVDTask extends DefaultTask {

    static String NAME = 'cleanAVD'
    String group = AMEBA_TEST
    String description = 'Cleans AVDs for emulators'

    @Inject
    private AndroidTestConfiguration testConf

    void cleanAVD() {
        ant.delete(dir: testConf.getAVDDir())
    }
}
