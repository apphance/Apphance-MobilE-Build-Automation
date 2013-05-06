package com.apphance.ameba.plugins.ios.framework

import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.framework.tasks.BuildFrameworkTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.plugins.ios.buildplugin.IOSPlugin.COPY_MOBILE_PROVISION_TASK_NAME
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME

/**
 * Plugin for preparing reports after successful IOS build.
 *
 */
class IOSFrameworkPlugin implements Plugin<Project> {

    public static final String BUILD_FRAMEWORK_TASK_NAME = 'buildFramework'

    @Inject
    private CommandExecutor executor

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        prepareBuildFrameworkTask()
    }

    private void prepareBuildFrameworkTask() {
        def task = project.task(BUILD_FRAMEWORK_TASK_NAME)
        task.group = AMEBA_BUILD
        task.description = 'Builds iOS framework project'
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME, COPY_MOBILE_PROVISION_TASK_NAME)
        task << { new BuildFrameworkTask(project, executor).buildIOSFramework() }
    }

    static public final String DESCRIPTION =
        """This plugins provides functionality of building shared framework for IOS projects.

While iOS itself provides a number of frameworks (shared libraries) that
can be used in various projects. It is undocumented feature of iOS that one can create own
framework. This plugin closes the gap.
"""


}
