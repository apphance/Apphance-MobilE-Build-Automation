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
    ProjectConfiguration conf
    @Inject
    ReleaseConfiguration releaseConf

    @TaskAction
    void clean() {
        releaseConf.otaDir.deleteDir()
        releaseConf.otaDir.mkdirs()
        conf.tmpDir.deleteDir()
        conf.tmpDir.mkdirs()
    }
}
