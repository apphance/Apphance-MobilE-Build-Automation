package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import org.gradle.api.GradleException
import org.gradle.api.Project

import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.retrieveProjectReleaseData

class VerifyReleaseNotesTask {

    private ProjectReleaseConfiguration releaseConf

    VerifyReleaseNotesTask(Project project) {
        this.releaseConf = retrieveProjectReleaseData(project)
    }

    public void verify() {
        if (!releaseConf.releaseNotes) {
            throw new GradleException("""Release notes of the project have not been set.... Please enter non-empty notes!\n
Either as -Prelease.notes='NOTES' gradle property or by setting RELEASE_NOTES environment variable""")
        }
    }
}
