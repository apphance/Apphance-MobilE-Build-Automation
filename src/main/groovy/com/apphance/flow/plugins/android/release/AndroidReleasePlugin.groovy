package com.apphance.flow.plugins.android.release

import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.plugins.android.release.tasks.AvailableArtifactsInfoTask
import com.apphance.flow.plugins.android.release.tasks.UpdateVersionTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static org.gradle.api.logging.Logging.getLogger

class AndroidReleasePlugin implements Plugin<Project> {

    def logger = getLogger(this.class)

    @Inject AndroidReleaseConfiguration releaseConf

    @Override
    void apply(Project project) {
        if (releaseConf.isEnabled()) {
            logger.lifecycle("Applying plugin ${this.class.simpleName}")

            project.task(
                    UpdateVersionTask.NAME,
                    type: UpdateVersionTask)

            project.task(
                    AvailableArtifactsInfoTask.NAME,
                    type: AvailableArtifactsInfoTask)
        }
    }
}
