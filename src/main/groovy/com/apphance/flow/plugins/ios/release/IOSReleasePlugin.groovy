package com.apphance.flow.plugins.ios.release

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.plugins.ios.release.tasks.AvailableArtifactsInfoTask
import com.apphance.flow.plugins.ios.release.tasks.UpdateVersionTask
import com.apphance.flow.plugins.project.tasks.CopySourcesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static org.gradle.api.logging.Logging.getLogger

/**
 * This plugin provides simple release functionality.
 * <br/><br/>
 * It provides basic release tasks, allowing to upgrade version of the application (in *.plist files)  while preparing
 * the release. Most importantly however, it produces ready-to-use OTA (Over-The-Air) package (under 'flow-ota'
 * directory). This folder contains all built artifacts (as well with HTML sites that present them) that can copied to
 * appropriate directory on a web server and become ready-to-use, easily installable OTA version of the application.
 */
class IOSReleasePlugin implements Plugin<Project> {

    private logger = getLogger(getClass())

    @Inject IOSReleaseConfiguration releaseConf

    @Override
    void apply(Project project) {
        if (releaseConf.isEnabled()) {
            logger.lifecycle("Applying plugin ${this.class.simpleName}")

            project.task(UpdateVersionTask.NAME,
                    type: UpdateVersionTask,
                    dependsOn: CopySourcesTask.NAME)

            project.task(AvailableArtifactsInfoTask.NAME,
                    type: AvailableArtifactsInfoTask)
        }
    }
}
