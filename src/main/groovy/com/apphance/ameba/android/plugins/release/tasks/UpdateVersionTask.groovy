package com.apphance.ameba.android.plugins.release.tasks

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.android.AndroidManifestHelper
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.*
import static org.gradle.api.logging.Logging.getLogger

class UpdateVersionTask {

    private l = getLogger(getClass())

    private Project project
    private ProjectConfiguration conf
    private AndroidManifestHelper manifestHelper = new AndroidManifestHelper()

    UpdateVersionTask(Project project) {
        this.project = project
        this.conf = getProjectConfiguration(project)
    }

    public void updateVersion() {
        conf.versionString = readPropertyOrEnvironmentVariable(project, 'version.string')
        conf.versionCode = readOptionalPropertyOrEnvironmentVariable(project, 'version.code') as Long
        manifestHelper.updateVersion(project.rootDir, new Expando(versionCode: conf.versionCode, versionString: conf.versionString))
        l.debug("New version code: $conf.versionCode")
        l.debug("Updated version string to: $conf.versionString")
        l.debug("Configuration : $conf")
    }
}
