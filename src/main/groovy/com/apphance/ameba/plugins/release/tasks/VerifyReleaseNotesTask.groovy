package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.configuration.ReleaseConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE

class VerifyReleaseNotesTask extends DefaultTask {

    static String NAME = 'verifyReleaseNotes'
    String group = AMEBA_RELEASE
    String description = 'Verifies that release notes are set for the build'

    @Inject
    private ReleaseConfiguration releaseConf

    @TaskAction
    void verify() {
        if (!releaseConf.releaseNotes) {
            throw new GradleException("""Release notes of the project have not been set.... Please enter non-empty notes!\n
Either as -Prelease.notes='NOTES' gradle property or by setting RELEASE_NOTES environment variable""")
        }
    }
}
