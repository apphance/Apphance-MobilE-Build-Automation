package com.apphance.ameba.plugins.release

import com.apphance.ameba.configuration.ReleaseConfiguration
import com.apphance.ameba.plugins.release.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging

import javax.inject.Inject

import static org.gradle.api.plugins.BasePlugin.CLEAN_TASK_NAME

/**
 *
 * This is Ameba release plugin.
 *
 * Plugin for releasing projects.
 *
 * The plugin provides all the basic tasks required to prepare OTA release of
 * an application. It should be added after build plugin is added.
 *
 */
class ProjectReleasePlugin implements Plugin<Project> {

    def l = Logging.getLogger(getClass())

    @Inject
    private ReleaseConfiguration releaseConf

    @Override
    void apply(Project project) {
        if (releaseConf.isEnabled()) {

            project.configurations.add('mail')
            project.dependencies {
                mail 'org.apache.ant:ant-javamail:1.9.0'
                mail 'javax.mail:mail:1.4'
                mail 'javax.activation:activation:1.1.1'
            }

            project.task(CopyGalleryFilesTask.NAME,
                    type: CopyGalleryFilesTask)

            project.task(PrepareForReleaseTask.NAME,
                    type: PrepareForReleaseTask,
                    dependsOn: [CopyGalleryFilesTask.NAME])

            project.task(ImageMontageTask.NAME,
                    type: ImageMontageTask,
                    dependsOn: [PrepareForReleaseTask.NAME])

            project.task(SendMailMessageTask.NAME,
                    type: SendMailMessageTask,
                    dependsOn: [PrepareForReleaseTask.NAME, 'prepareMailMessage'])

            project.task(CleanReleaseTask.NAME,
                    type: CleanReleaseTask,
                    dependsOn: [CLEAN_TASK_NAME])

            project.task(BuildSourcesZipTask.NAME,
                    type: BuildSourcesZipTask,
                    dependsOn: [PrepareForReleaseTask.NAME])

        }
    }
}