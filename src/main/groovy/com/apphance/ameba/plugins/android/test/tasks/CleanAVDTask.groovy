package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.plugins.android.test.AndroidTestConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.plugins.android.test.AndroidTestConfigurationRetriever.getAndroidTestConfiguration

class CleanAVDTask {

    private AntBuilder ant
    private AndroidTestConfiguration androidTestConf

    CleanAVDTask(Project project) {
        this.ant = project.ant
        this.androidTestConf = getAndroidTestConfiguration(project)
    }

    void cleanAVD() {
        ant.delete(dir: androidTestConf.avdDir)
    }
}
