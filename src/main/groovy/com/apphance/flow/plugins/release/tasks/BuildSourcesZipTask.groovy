package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.release.ReleaseConfiguration
import com.apphance.flow.plugins.release.FlowArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.configuration.ProjectConfiguration.*
import static com.apphance.flow.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME
import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE
import static com.apphance.flow.util.file.FileManager.removeMissingSymlinks

class BuildSourcesZipTask extends DefaultTask {

    static String NAME = 'buildSourcesZip'
    String description = 'Builds sources .zip file.'
    String group = FLOW_RELEASE

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
                exclude(name: "${BUILD_DIR}/**")
                exclude(name: "${OTA_DIR}/**")
                exclude(name: "${TMP_DIR}/**")
                exclude(name: "${LOG_DIR}/**")
                exclude(name: '**/buildSrc/build/**')
                exclude(name: '**/build.gradle')
                exclude(name: '**/gradle.properties')
                exclude(name: "**/$FLOW_PROP_FILENAME")
                exclude(name: '**/.gradle/**')
                conf.sourceExcludes.each { exclude(name: it) }
            }
        }
        logger.debug("Extra source excludes: $conf.sourceExcludes")
        logger.debug("Created source files at: $destZip")
    }

    private void prepareSourcesAndDocumentationArtifacts() {
        releaseConf.sourcesZip = new FlowArtifact(
                name: 'Zipped project sources',
                url: null, // we do not publish
                location: new File(releaseConf.releaseDir, "${conf.projectName.value}-${conf.fullVersionString}-src.zip"))
    }
}
