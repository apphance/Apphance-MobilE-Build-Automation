package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.getProjectReleaseConfiguration
import static org.gradle.api.logging.Logging.getLogger

class BuildDocZipTask {

    private l = getLogger(getClass())
    private Project project
    private ProjectReleaseConfiguration releaseConf

    BuildDocZipTask(Project project) {
        this.project = project
        this.releaseConf = getProjectReleaseConfiguration(project)
    }

    public void buildDocZip() {
        File destZip = releaseConf.documentationZip.location
        destZip.mkdirs()
        destZip.delete()
        File javadocDir = project.file('build/docs/javadoc')
        project.ant.zip(destfile: destZip, basedir: javadocDir)
        l.debug("Zipped documentation written to: $destZip.absolutePath")
    }
}
