package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.ReleaseConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE

class PrepareForReleaseTask extends DefaultTask {

    static final String NAME = 'prepareForRelease'
    String group = AMEBA_RELEASE
    String description = 'Prepares project for release'

    @Inject
    private ProjectConfiguration projectConf
    @Inject
    private ReleaseConfiguration releaseConf

    @TaskAction
    void prepare() {
        prepareSourcesAndDocumentationArtifacts()
        prepareMailArtifacts()
    }

    private prepareSourcesAndDocumentationArtifacts() {
        def sourceZipName = projectConf.projectVersionedName + "-src.zip"
        releaseConf.sourcesZip = new AmebaArtifact(
                name: "$projectConf.projectName.value-src",
                url: null, // we do not publish
                location: new File(projectConf.tmpDir, sourceZipName))
        releaseConf.targetDirectory.mkdirs()
    }

    private prepareMailArtifacts() {
        releaseConf.mailMessageFile = new AmebaArtifact(
                name: 'Mail message file',
                url: new URL(releaseConf.versionedApplicationUrl, 'message_file.html'),
                location: new File(releaseConf.targetDirectory, 'message_file.html'))
    }
}

