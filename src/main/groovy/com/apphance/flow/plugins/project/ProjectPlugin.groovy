package com.apphance.flow.plugins.project

import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import com.apphance.flow.plugins.project.tasks.PrepareSetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import static com.apphance.flow.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME
import static org.gradle.api.logging.Logging.getLogger

/**
 * This is the base plugin which is applied to every project.
 * <br/><br/>
 * This plugin provides setup-related tasks. The tasks allow to generate new configuration or verify existing
 * configuration.
 */
class ProjectPlugin implements Plugin<Project> {

    private logger = getLogger(getClass())

    public static final String COPY_SOURCES_TASK_NAME = 'copySources'

    @Override
    void apply(Project project) {
        logger.lifecycle("Applying plugin ${this.class.simpleName}")

        project.repositories.mavenCentral()

        if (project.file(FLOW_PROP_FILENAME).exists()) {
            project.task(CleanFlowTask.NAME, type: CleanFlowTask)
        }

        project.task(PrepareSetupTask.NAME, type: PrepareSetupTask)
    }
}