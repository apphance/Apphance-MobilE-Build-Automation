package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.plugins.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.plugins.android.AndroidSingleVariantJarBuilder
import com.apphance.ameba.plugins.android.release.tasks.AvailableArtifactsInfoTask
import com.apphance.ameba.plugins.android.release.tasks.BuildDocZipTask
import com.apphance.ameba.plugins.android.release.tasks.MailMessageTask
import com.apphance.ameba.plugins.android.release.tasks.UpdateVersionTask
import com.apphance.ameba.plugins.release.tasks.PrepareForReleaseTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static org.gradle.api.plugins.JavaPlugin.JAVADOC_TASK_NAME

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

    @Inject
    private AndroidConfiguration conf
    @Inject
    private AndroidReleaseConfiguration releaseConf

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
                    BuildDocZipTask.NAME,
                    type: BuildDocZipTask,
                    dependsOn: [JAVADOC_TASK_NAME, PrepareForReleaseTask.NAME])
            project.task(
                    MailMessageTask.NAME,
                    type: MailMessageTask,
                    dependsOn: [AvailableArtifactsInfoTask.NAME, PrepareForReleaseTask.NAME])

            //TODO to be separated, refactored, redesigned :/
            AndroidSingleVariantApkBuilder.buildListeners << new AndroidReleaseApkListener(project, conf, releaseConf)
            AndroidSingleVariantJarBuilder.buildListeners << new AndroidReleaseJarListener(project, conf, releaseConf)
        }
    }
}
