package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.ReleaseConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.util.file.FileManager.removeMissingSymlinks
import static org.gradle.api.logging.Logging.getLogger

class BuildSourcesZipTask extends DefaultTask {

    private l = getLogger(getClass())

    static String NAME = 'buildSourcesZip'
    String description = 'Builds sources .zip file.'
    String group = AMEBA_RELEASE

    @Inject
    ProjectConfiguration conf
    @Inject
    ReleaseConfiguration releaseConf

    @TaskAction
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
