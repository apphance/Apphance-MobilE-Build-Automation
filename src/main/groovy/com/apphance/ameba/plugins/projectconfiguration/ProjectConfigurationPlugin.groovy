package com.apphance.ameba.plugins.projectconfiguration

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.reader.ConversationManager
import com.apphance.ameba.configuration.reader.PropertyPersister
import com.apphance.ameba.plugins.projectconfiguration.tasks.CheckTestsTask
import com.apphance.ameba.plugins.projectconfiguration.tasks.CleanConfTask
import com.apphance.ameba.plugins.projectconfiguration.tasks.VerifySetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_SETUP
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
class ProjectConfigurationPlugin implements Plugin<Project> {

    def l = getLogger(getClass())

    public final static String PROJECT_NAME_PROPERTY = 'project.name'
    public final static String PREPARE_SETUP_TASK_NAME = 'prepareSetup'
    public final static String VERIFY_SETUP_TASK_NAME = 'verifySetup'

    @Inject
    private Map<Integer, AbstractConfiguration> configurations

    @Inject
    PropertyPersister propertyPersister

    private Project project

    @Inject
    ConversationManager conversationManager

    @Override
    void apply(Project project) {
        this.project = project

        addMavenRepository()

        prepareSetupTask()

        project.task(CleanConfTask.NAME, type: CleanConfTask)
        project.task(CheckTestsTask.NAME, type: CheckTestsTask)

        project.task(VerifySetupTask.NAME, type: VerifySetupTask)
    }

    private void prepareSetupTask() {
        project.task('prepareSetup',
                group: AMEBA_SETUP,
                description: 'Prepares configuration (ameba.properties)') << {
            Collection<AbstractConfiguration> sorted = configurations.sort().values()
            conversationManager.resolveConfigurations(sorted)
            propertyPersister.save(sorted)
        }
    }

    private void addMavenRepository() {
        project.repositories.mavenCentral()
    }
}