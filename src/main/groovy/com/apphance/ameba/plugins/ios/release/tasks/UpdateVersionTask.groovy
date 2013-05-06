package com.apphance.ameba.plugins.ios.release.tasks

import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.PropertyCategory.readPropertyOrEnvironmentVariable
import static com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever.getIosProjectConfiguration
import static org.gradle.api.logging.Logging.getLogger

class UpdateVersionTask {

    private l = getLogger(getClass())

    private Project project
    private ProjectConfiguration conf
    private IOSProjectConfiguration iosConf
    private IOSPlistProcessor iosPlistProcessor = new IOSPlistProcessor()

    UpdateVersionTask(Project project) {
        this.project = project
        this.conf = getProjectConfiguration(project)
        this.iosConf = getIosProjectConfiguration(project)
    }

    void updateVersion() {
        conf.versionString = readPropertyOrEnvironmentVariable(project, 'version.string')
        conf.versionCode = readPropertyOrEnvironmentVariable(project, 'version.code') as Long
        iosPlistProcessor.incrementPlistVersion(iosConf, conf)
        l.lifecycle("New version code: ${conf.versionCode}")
        l.lifecycle("Updated version string to ${conf.versionString}")
    }
}
