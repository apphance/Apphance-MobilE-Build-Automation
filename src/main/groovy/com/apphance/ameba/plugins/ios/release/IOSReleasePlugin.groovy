package com.apphance.ameba.plugins.ios.release

import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.apphance.ameba.plugins.ios.buildplugin.tasks.CopySourcesTask
import com.apphance.ameba.plugins.ios.release.tasks.AvailableArtifactsInfoTask
import com.apphance.ameba.plugins.ios.release.tasks.PrepareMailMessageTask
import com.apphance.ameba.plugins.ios.release.tasks.UpdateVersionTask
import com.apphance.ameba.plugins.project.tasks.CheckTestsTask
import com.apphance.ameba.plugins.project.tasks.CleanFlowTask
import com.apphance.ameba.plugins.project.tasks.PrepareSetupTask
import com.apphance.ameba.plugins.project.tasks.VerifySetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

/**
 *
 * Plugin for releasing iOS build.
 *
 * This is the plugin that provides simple release functionality.
 *
 * It provides basic release tasks, so that you can upgrade version of the application
 * while preparing the release and it provides post-release tasks that commit it into the repository.
 * Most importantly however, it produces ready-to-use OTA (Over-The-Air) package (in ota directory)
 * that you can copy to appropriate directory on your web server and have ready-to-use,
 * easily installable OTA version of your application.
 *
 */
class IOSReleasePlugin implements Plugin<Project> {

    @Inject IOSReleaseConfiguration releaseConf
    @Inject IOSSingleVariantBuilder builder
    @Inject IOSReleaseListener listener

    @Override
    void apply(Project project) {
        if (releaseConf.isEnabled()) {

            project.task(UpdateVersionTask.NAME,
                    type: UpdateVersionTask)

            project.task(AvailableArtifactsInfoTask.NAME,
                    type: AvailableArtifactsInfoTask)

            project.task(PrepareMailMessageTask.NAME,
                    type: PrepareMailMessageTask,
                    dependsOn: AvailableArtifactsInfoTask.NAME)

            builder.registerListener(listener)

            project.tasks.each {
                if (!(it.name in [VerifySetupTask.NAME, PrepareSetupTask.NAME, CopySourcesTask.NAME, CleanFlowTask.NAME, CheckTestsTask.NAME])) {
                    it.dependsOn VerifySetupTask.NAME
                }
            }
        }
    }
}
