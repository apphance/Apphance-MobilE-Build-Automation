package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.retrieveProjectReleaseData

class CleanReleaseTask {

    private ProjectConfiguration conf
    private ProjectReleaseConfiguration releaseConf

    CleanReleaseTask(Project project) {
        this.conf = getProjectConfiguration(project)
        this.releaseConf = retrieveProjectReleaseData(project)
    }

    void clean() {
        releaseConf.otaDirectory.deleteDir()
        conf.tmpDirectory.deleteDir()
        releaseConf.otaDirectory.mkdirs()
        conf.tmpDirectory.mkdirs()
    }
}
