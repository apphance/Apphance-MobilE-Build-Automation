package com.apphance.ameba.plugins.project

import com.apphance.ameba.plugins.project.tasks.CheckTestsTask
import com.apphance.ameba.plugins.project.tasks.CleanConfTask
import com.apphance.ameba.plugins.project.tasks.VerifySetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

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

    @Override
    void apply(Project project) {
        project.repositories.mavenCentral()

        project.task(CleanConfTask.NAME, type: CleanConfTask)
        project.task(CheckTestsTask.NAME, type: CheckTestsTask)
        project.task(PrepareSetupTask.NAME, type: PrepareSetupTask)
        project.task(VerifySetupTask.NAME, type: VerifySetupTask)
    }
}