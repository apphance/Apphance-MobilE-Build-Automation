package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.release.ReleaseConfiguration
import com.apphance.ameba.plugins.release.AmebaArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE

class PrepareForReleaseTask extends DefaultTask {

    static final String NAME = 'prepareForRelease'
    String group = AMEBA_RELEASE
    String description = 'Prepares project for release'

    @Inject ProjectConfiguration conf
    @Inject ReleaseConfiguration releaseConf

    @TaskAction
    void prepare() {
        prepareMailArtifacts()
    }

    private prepareMailArtifacts() {
        releaseConf.mailMessageFile = new AmebaArtifact(
                name: 'Mail message file',
                url: new URL(releaseConf.versionedApplicationUrl, 'message_file.html'),
                location: new File(releaseConf.targetDir, 'message_file.html'))
    }
}

