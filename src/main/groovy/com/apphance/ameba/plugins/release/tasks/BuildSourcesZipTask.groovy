package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.retrieveProjectReleaseData
import static com.apphance.ameba.util.file.FileManager.removeMissingSymlinks
import static org.gradle.api.logging.Logging.getLogger

class BuildSourcesZipTask {

    private l = getLogger(getClass())

    private Project project
    private ProjectConfiguration conf
    private ProjectReleaseConfiguration releaseConf
    private AntBuilder ant

    BuildSourcesZipTask(Project project) {
        this.project = project
        this.ant = project.ant
        this.conf = getProjectConfiguration(project)
        this.releaseConf = retrieveProjectReleaseData(project)
    }

    void buildSourcesZip() {
        File destZip = releaseConf.sourcesZip.location
        l.lifecycle('Removing empty symlinks')
        removeMissingSymlinks(project.rootDir)
        destZip.parentFile.mkdirs()
        destZip.delete()
        l.debug('Compressing sources')
        ant.zip(destfile: destZip) {
            fileset(dir: project.rootDir) {
                exclude(name: 'build/**')
                exclude(name: 'ota/**')
                exclude(name: 'tmp/**')
                exclude(name: '**/buildSrc/build/**')
                exclude(name: '**/build.gradle')
                exclude(name: '**/gradle.properties')
                exclude(name: '**/.gradle/**')
                conf.sourceExcludes.each { exclude(name: it) }
            }
        }
        l.debug("Extra source excludes: $conf.sourceExcludes")
        l.debug("Created source files at: $destZip")
    }
}
