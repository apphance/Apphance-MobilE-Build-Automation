package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.plugins.android.builder.AndroidSingleVariantApkBuilder
import com.apphance.ameba.plugins.android.builder.AndroidSingleVariantJarBuilder
import com.apphance.ameba.plugins.android.release.tasks.AvailableArtifactsInfoTask
import com.apphance.ameba.plugins.android.release.tasks.PrepareMailMessageTask
import com.apphance.ameba.plugins.android.release.tasks.UpdateVersionTask
import com.apphance.ameba.plugins.release.tasks.PrepareForReleaseTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

/**
 * Plugin that provides release functionality for android.<br><br>
 *
 * It provides basic release tasks, so that you can upgrade version of the application
 * while preparing the release and it provides post-release tasks that commit it into the repository.
 * Most importantly however, it produces ready-to-use OTA (Over-The-Air) package (in ota directory)
 * that you can copy to appropriate directory on your web server and have ready-to-use,
 * easily installable OTA version of your application.|
 */
class AndroidReleasePlugin implements Plugin<Project> {

    @Inject AndroidReleaseConfiguration releaseConf
    @Inject AndroidSingleVariantApkBuilder apkBuilder
    @Inject AndroidReleaseApkListener apkListener
    @Inject AndroidSingleVariantJarBuilder jarBuilder
    @Inject AndroidReleaseJarListener jarListener

    @Override
    void apply(Project project) {
        if (releaseConf.isEnabled()) {

            project.task(
                    UpdateVersionTask.NAME,
                    type: UpdateVersionTask)

            project.task(
                    AvailableArtifactsInfoTask.NAME,
                    type: AvailableArtifactsInfoTask)

            project.task(
                    PrepareMailMessageTask.NAME,
                    type: PrepareMailMessageTask,
                    dependsOn: [AvailableArtifactsInfoTask.NAME, PrepareForReleaseTask.NAME])

            apkBuilder.registerListener(apkListener)
            jarBuilder.registerListener(jarListener)
        }
    }
}
