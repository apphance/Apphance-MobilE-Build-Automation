package com.apphance.ameba.plugins.projectconfiguration

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Plugin for basic project configuration.
 *
 */
class ProjectConfigurationPlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(ProjectConfigurationPlugin.class)

    public static final String PROJECT_NAME_PROPERTY = 'project.name'

    ProjectHelper projectHelper
    ProjectConfiguration conf

    void apply(Project project) {
        projectHelper = new ProjectHelper()
        conf = PropertyCategory.getProjectConfiguration(project)
        project.convention.plugins.put('amebaPropertyDefaults', new ProjectConfigurationConvention())
        prepareRepositories(project)
        readProjectConfigurationTask(project)
        project.task('prepareSetup', type: PrepareSetupTask.class)
        project.task('verifySetup', type: VerifySetupTask.class)
        project.task('showSetup', type: ShowSetupTask.class)
        project.task('checkTests', type: CheckTestsTask.class)
        project.task('showConventions', type: ShowConventionsTask.class)
        prepareShowConventionRule(project)
        prepareCleanConfigurationTask(project)
    }

    private void prepareShowConventionRule(Project project) {
        project.tasks.addRule("Pattern: showConvention<ConventionName>: Shows current convention values for convention specified by name") { String taskName ->
            if (taskName.startsWith("showConvention")) {
                project.task(taskName) << {
                    String pluginName = taskName.substring('showConvention'.length()).replaceAll('^.') { it.toLowerCase() }
                    def pluginConventionObject = project.convention.plugins[pluginName]
                    if (pluginConventionObject == null) {
                        throw new GradleException("There is no convention with ${pluginName} name")
                    }
                    StringBuilder sb = new StringBuilder()
                    new ShowConventionHelper().getConventionObject(sb, pluginName, pluginConventionObject)
                    println sb
                }
            }
        }
    }

    void prepareRepositories(Project project) {
        project.repositories.mavenCentral()
    }

    def void readProjectConfigurationTask(Project project) {
        def task = project.task('readProjectConfiguration')
        task.description = "Reads project's configuration and sets it up in projectConfiguration property of project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task << {
            // NOTE! conf.versionString and conf.versionCode need to
            // be read before project configuration task -> task reading the version
            // should be injected here
            this.conf = PropertyCategory.retrieveBasicProjectData(project)
        }
    }

    def void prepareCleanConfigurationTask(Project project) {
        def task = project.task('cleanConfiguration')
        task.description = "Cleans configuration before each build"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
        task << {
            conf.buildDirectory.deleteDir()
            conf.tmpDirectory.deleteDir()
            conf.logDirectory.deleteDir()
            conf.buildDirectory.mkdirs()
            conf.logDirectory.mkdirs()
            conf.tmpDirectory.mkdirs()
        }
        task.dependsOn(project.readProjectConfiguration)
    }


    static public final String DESCRIPTION =
        """This is the base plugin which should be applied by any project.

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