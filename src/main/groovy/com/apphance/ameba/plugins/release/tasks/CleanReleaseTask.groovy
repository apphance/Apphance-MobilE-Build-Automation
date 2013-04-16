package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.ReleaseConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE

class CleanReleaseTask extends DefaultTask {

    static String NAME = 'cleanRelease'
    String description = 'Cleans release related directories'
    String group = AMEBA_RELEASE

    @Inject
    private ProjectConfiguration conf
    @Inject
    private ReleaseConfiguration releaseConf

    @TaskAction
    void clean() {
        releaseConf.otaDirectory.deleteDir()
        conf.tmpDir.value.deleteDir()
        releaseConf.otaDirectory.mkdirs()
        conf.tmpDir.value.mkdirs()
    }
}
