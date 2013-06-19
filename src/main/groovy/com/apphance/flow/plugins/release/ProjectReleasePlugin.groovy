package com.apphance.flow.plugins.release

import com.apphance.flow.configuration.release.ReleaseConfiguration
import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import com.apphance.flow.plugins.release.tasks.BuildSourcesZipTask
import com.apphance.flow.plugins.release.tasks.ImageMontageTask
import com.apphance.flow.plugins.release.tasks.SendMailMessageTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static org.gradle.api.logging.Logging.getLogger

/**
 *
 * This is Flow release plugin.
 *
 * Plugin for releasing projects.
 *
 * The plugin provides all the basic tasks required to prepare OTA release of
 * an application. It should be added after build plugin is added.
 *
 */
class ProjectReleasePlugin implements Plugin<Project> {

    def logger = getLogger(getClass())

    @Inject ReleaseConfiguration releaseConf

    @Override
    void apply(Project project) {
        if (releaseConf.isEnabled()) {
            logger.lifecycle("Applying plugin ${this.class.simpleName}")

            project.configurations.create('mail')
            project.dependencies {
                mail 'org.apache.ant:ant-javamail:1.9.0'
                mail 'javax.mail:mail:1.4'
                mail 'javax.activation:activation:1.1.1'
            }

            project.task(ImageMontageTask.NAME,
                    type: ImageMontageTask)

            project.task(SendMailMessageTask.NAME,
                    type: SendMailMessageTask,
                    dependsOn: 'prepareAvailableArtifactsInfo')

            project.task(BuildSourcesZipTask.NAME,
                    type: BuildSourcesZipTask)

            project.tasks.findByName(CleanFlowTask.NAME) << {
                releaseConf.otaDir.deleteDir()
                releaseConf.otaDir.mkdirs()
            }
        }
    }
}