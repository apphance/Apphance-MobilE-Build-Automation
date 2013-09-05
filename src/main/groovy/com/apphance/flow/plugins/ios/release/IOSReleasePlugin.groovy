package com.apphance.flow.plugins.ios.release

import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.plugins.ios.buildplugin.tasks.CopySourcesTask
import com.apphance.flow.plugins.ios.release.tasks.AvailableArtifactsInfoTask
import com.apphance.flow.plugins.ios.release.tasks.UpdateVersionTask
import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import com.apphance.flow.plugins.project.tasks.PrepareSetupTask
import com.apphance.flow.plugins.project.tasks.VerifySetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static org.gradle.api.logging.Logging.getLogger

class IOSReleasePlugin implements Plugin<Project> {

    private logger = getLogger(getClass())

    @Inject IOSReleaseConfiguration releaseConf

    @Override
    void apply(Project project) {
        if (releaseConf.isEnabled()) {
            logger.lifecycle("Applying plugin ${this.class.simpleName}")

            project.task(UpdateVersionTask.NAME,
                    type: UpdateVersionTask)

            project.task(AvailableArtifactsInfoTask.NAME,
                    type: AvailableArtifactsInfoTask)

            project.tasks.each {
                if (!(it.name in [VerifySetupTask.NAME, PrepareSetupTask.NAME, CopySourcesTask.NAME, CleanFlowTask.NAME])) {
                    it.dependsOn VerifySetupTask.NAME
                }
            }
        }
    }
}
