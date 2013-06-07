package com.apphance.flow.plugins.android.release

import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.plugins.android.release.tasks.AvailableArtifactsInfoTask
import com.apphance.flow.plugins.android.release.tasks.PrepareMailMessageTask
import com.apphance.flow.plugins.android.release.tasks.UpdateVersionTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static org.gradle.api.logging.Logging.getLogger

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

    def log = getLogger(this.class)

    @Inject AndroidReleaseConfiguration releaseConf

    @Override
    void apply(Project project) {
        if (releaseConf.isEnabled()) {
            log.lifecycle("Applying plugin ${this.class.simpleName}")

            project.task(
                    UpdateVersionTask.NAME,
                    type: UpdateVersionTask)

            project.task(
                    AvailableArtifactsInfoTask.NAME,
                    type: AvailableArtifactsInfoTask)

            project.task(
                    PrepareMailMessageTask.NAME,
                    type: PrepareMailMessageTask,
                    dependsOn: AvailableArtifactsInfoTask.NAME)
        }
    }
}
