package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.plugins.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.plugins.android.AndroidSingleVariantJarBuilder
import com.apphance.ameba.plugins.android.release.tasks.AvailableArtifactsInfoTask
import com.apphance.ameba.plugins.android.release.tasks.BuildDocZipTask
import com.apphance.ameba.plugins.android.release.tasks.MailMessageTask
import com.apphance.ameba.plugins.android.release.tasks.UpdateVersionTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.plugins.android.buildplugin.AndroidPlugin.READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.PREPARE_FOR_RELEASE_TASK_NAME
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
    private AndroidReleaseConfiguration releaseConf
    @Inject
    private AndroidReleaseApkListener androidReleaseApkListener
    @Inject
    private AndroidReleaseJarListener androidReleaseJarListener

    @Override
    void apply(Project project) {
        if (releaseConf.isEnabled()) {

            project.task(
                    UpdateVersionTask.NAME,
                    type: UpdateVersionTask,
                    dependsOn: READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
            project.task(
                    AvailableArtifactsInfoTask.NAME,
                    type: AvailableArtifactsInfoTask,
                    dependsOn: READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
            project.task(
                    BuildDocZipTask.NAME,
                    type: BuildDocZipTask,
                    dependsOn: [JAVADOC_TASK_NAME, READ_PROJECT_CONFIGURATION_TASK_NAME, PREPARE_FOR_RELEASE_TASK_NAME])
            project.task(
                    MailMessageTask.NAME,
                    type: MailMessageTask,
                    dependsOn: [AvailableArtifactsInfoTask.NAME, READ_PROJECT_CONFIGURATION_TASK_NAME, PREPARE_FOR_RELEASE_TASK_NAME])

            //TODO to be separated, refactored, redesigned :/
            AndroidSingleVariantApkBuilder.buildListeners << androidReleaseApkListener
            AndroidSingleVariantJarBuilder.buildListeners << androidReleaseJarListener
        }
    }
}
