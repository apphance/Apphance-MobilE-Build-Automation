package com.apphance.ameba.plugins.project

import com.apphance.ameba.plugins.project.tasks.CleanFlowTask
import com.apphance.ameba.plugins.project.tasks.PrepareSetupTask
import com.apphance.ameba.plugins.project.tasks.VerifySetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import static com.apphance.ameba.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME
import static org.gradle.api.logging.Logging.getLogger

/**
 * This is the base plugin which should be applied in any project.
 *
 * The plugin should be applied before any other Ameba plugin. It reads basic project configuration and loads shared
 * configuration for all other plugins.
 *
 * This plugin provides setup-related tasks. The tasks allow to generate new configuration,
 * verify existing configuration and show the configuration to the user.
 * It also adds several utility tasks that can be used across all types of projects.
 *
 * Conventions defined in this task are used to provide default values for properties from all other plugins.
 * Defining such defaults and importing them from your shared location is the easiest way to provide
 * organisation-specific defaults across your projects.
 */
class ProjectPlugin implements Plugin<Project> {

    private log = getLogger(getClass())

    public static final String COPY_SOURCES_TASK_NAME = 'copySources'

    @Override
    void apply(Project project) {
        log.lifecycle("Applying plugin ${this.class.simpleName}")

        project.repositories.mavenCentral()

        if (project.file(FLOW_PROP_FILENAME).exists()) {
            project.task(CleanFlowTask.NAME, type: CleanFlowTask)

            project.task(VerifySetupTask.NAME,
                    type: VerifySetupTask,
                    dependsOn: COPY_SOURCES_TASK_NAME)

        }

        project.task(PrepareSetupTask.NAME, type: PrepareSetupTask)
    }
}