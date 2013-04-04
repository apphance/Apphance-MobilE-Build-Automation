package com.apphance.ameba.plugins.projectconfiguration

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.configuration.ConversationManager
import com.apphance.ameba.configuration.PropertyPersister
import com.apphance.ameba.plugins.projectconfiguration.tasks.*
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import javax.inject.Inject

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.PropertyCategory.retrieveBasicProjectData
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
import static org.gradle.api.logging.Logging.getLogger

/**
 * Plugin for basic project configuration.
 *
 */
class ProjectConfigurationPlugin implements Plugin<Project> {

    def l = getLogger(getClass())

    public final static String PROJECT_NAME_PROPERTY = 'project.name'

    public final static String READ_PROJECT_CONFIGURATION_TASK_NAME = 'readProjectConfiguration'
    public final static String CLEAN_CONFIGURATION_TASK_NAME = 'cleanConfiguration'
    public final static String PREPARE_SETUP_TASK_NAME = 'prepareSetup'
    public final static String VERIFY_SETUP_TASK_NAME = 'verifySetup'
    public final static String SHOW_CONVENTIONS_TASK_NAME = 'showConventions'
    public final static String SHOW_SETUP_TASK_NAME = 'showSetup'
    public final static String CHECK_TESTS_TASK_NAME = 'checkTests'

    public final static String AMEBA_PROPERTY_DEFAULTS_CONVENTION_NAME = 'amebaPropertyDefaults'

    @Inject
    Map<Integer, Configuration> configurations

    @Inject
    PropertyPersister propertyPersister

    private Project project

    @Inject
    ConversationManager conversationManager

    @Override
    void apply(Project project) {
        this.project = project

        addAmebaConvention()
        addMavenRepository()
        prepareReadProjectConfigurationTask()
        prepareCleanConfigurationTask()
        prepareShowConventionRule()

        prepareSetupTask2()

        project.task(PREPARE_SETUP_TASK_NAME, type: PrepareSetupTask)
        project.task(VERIFY_SETUP_TASK_NAME, type: VerifySetupTask)
        project.task(SHOW_SETUP_TASK_NAME, type: ShowSetupTask)
        project.task(CHECK_TESTS_TASK_NAME, type: CheckTestsTask)
        project.task(SHOW_CONVENTIONS_TASK_NAME, type: ShowConventionsTask)
    }

    private void prepareSetupTask2() {
        def task = project.task('prepareSetup2')
        task.group = 'conf group'
        task.description = 'Prepares configuration (ameba.properties)'
        task << {
            Collection<Configuration> sorted = configurations.sort().values()
            conversationManager.resolveConfigurations(sorted)
            propertyPersister.save(sorted)
        }
    }

    private void addAmebaConvention() {
        project.convention.plugins.put(AMEBA_PROPERTY_DEFAULTS_CONVENTION_NAME, new ProjectConfigurationConvention())
    }

    private void addMavenRepository() {
        project.repositories.mavenCentral()
    }

    private void prepareReadProjectConfigurationTask() {
        Task task = project.task(READ_PROJECT_CONFIGURATION_TASK_NAME)
        task.description = "Reads project's configuration and sets it up in projectConfiguration property of project"
        task.group = AMEBA_CONFIGURATION
        task.doLast { retrieveBasicProjectData(project) }
    }

    private void prepareCleanConfigurationTask() {
        Task task = project.task(CLEAN_CONFIGURATION_TASK_NAME)
        task.description = 'Cleans configuration before each build'
        task.group = AMEBA_CONFIGURATION
        task.doLast { new CleanConfTask(getProjectConfiguration(project)).clean() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)
    }

    //TODO to separate?
    private void prepareShowConventionRule() {
        project.tasks.addRule("Pattern: showConvention<ConventionName>: Shows current convention values for convention specified by name") { String taskName ->
            if (taskName.startsWith("showConvention")) {
                project.task(taskName) << {
                    String pluginName = taskName.substring('showConvention'.length()).replaceAll('^.') { it.toLowerCase() }
                    def pluginConventionObject = project.convention.plugins[pluginName]
                    if (!pluginConventionObject) {
                        throw new GradleException("There is no convention with ${pluginName} name")
                    }
                    StringBuilder sb = new StringBuilder()
                    new ShowConventionHelper().getConventionObject(sb, pluginName, pluginConventionObject)
                    l.lifecycle(sb.toString())
                }
            }
        }
    }


    static public final String DESCRIPTION =
        """This is the base plugin which should be applied in any project.

The plugin should be applied before any other Ameba plugin. It reads basic project configuration and loads shared
configuration for all other plugins.

This plugin provides setup-related tasks. The tasks allow to generate new configuration,
verify existing configuration and show the configuration to the user.
It also adds several utility tasks that can be used across all types of projects.

Conventions defined in this task are used to provide default values for properties from all other plugins.
Defining such defaults and importing them from your shared location is the easiest way to provide
organisation-specific defaults across your projects.

"""

    static class ProjectConfigurationConvention {
        static public final String DESCRIPTION =
            """Using this convention object you can specify different defaults
for properties (for all properties from all plugins). You need to specify it as string map of the form
"['property.name' : 'value' ]". Note that you can use groovy to calculate values and use dynamically
calculated values in this map.
"""
        def String defaults = '[:]'

        def amebaPropertyDefaults(Closure close) {
            close.delegate = this
            close.run()
        }
    }
}