package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.release.ReleaseConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.ameba.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME
import static com.apphance.ameba.configuration.release.ReleaseConfiguration.OTA_DIR
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.util.file.FileManager.removeMissingSymlinks
import static org.gradle.api.logging.Logging.getLogger

class BuildSourcesZipTask extends DefaultTask {

    private l = getLogger(getClass())

    static String NAME = 'buildSourcesZip'
    String description = 'Builds sources .zip file.'
    String group = AMEBA_RELEASE

    @Inject ProjectConfiguration conf
    @Inject ReleaseConfiguration releaseConf

    @TaskAction
    void buildSourcesZip() {

        prepareSourcesAndDocumentationArtifacts()

        removeMissingSymlinks(conf.rootDir)

        File destZip = releaseConf.sourcesZip.location
        destZip.parentFile.mkdirs()
        destZip.delete()

        ant.zip(destfile: destZip) {
            fileset(dir: conf.rootDir) {
                exclude(name: 'build/**')
                exclude(name: "${OTA_DIR}/**")
                exclude(name: "${TMP_DIR}/**")
                exclude(name: '**/buildSrc/build/**')
                exclude(name: '**/build.gradle')
                exclude(name: '**/gradle.properties')
                exclude(name: "**/$FLOW_PROP_FILENAME")
                exclude(name: '**/.gradle/**')
                conf.sourceExcludes.each { exclude(name: it) }
            }
        }
        l.debug("Extra source excludes: $conf.sourceExcludes")
        l.debug("Created source files at: $destZip")
    }

    private void prepareSourcesAndDocumentationArtifacts() {
        def sourceZipName = "$conf.projectVersionedName-src.zip"
        releaseConf.sourcesZip = new AmebaArtifact(
                name: "$conf.projectName.value-src",
                url: null, // we do not publish
                location: new File(conf.tmpDir, sourceZipName))
    }
}
