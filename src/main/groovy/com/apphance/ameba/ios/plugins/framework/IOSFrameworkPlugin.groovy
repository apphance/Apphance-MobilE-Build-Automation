package com.apphance.ameba.ios.plugins.framework

import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import javax.inject.Inject

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_BUILD

/**
 * Plugin for preparing reports after successful IOS build.
 *
 */
class IOSFrameworkPlugin implements Plugin<Project> {
    static Logger logger = Logging.getLogger(IOSFrameworkPlugin.class)

    @Inject
    CommandExecutor executor

    def void apply(Project project) {

        def task = project.task('buildFramework',
                group: AMEBA_BUILD,
                description: 'Builds iOS framework project',
                dependsOn: [project.readProjectConfiguration, project.copyMobileProvision]
        )
        task.doLast { new IOSFrameworkBuilder(project, executor).buildIOSFramework() }

        project.prepareSetup.prepareSetupOperations << new PrepareFrameworkSetupOperation()
        project.verifySetup.verifySetupOperations << new VerifyFrameworkSetupOperation()
        project.showSetup.showSetupOperations << new ShowFrameworkSetupOperation()
    }

    static public final String DESCRIPTION =
        """This plugins provides functionality of building shared framework for IOS projects.

While iOS itself provides a number of frameworks (shared libraries) that
can be used in various projects. It is undocumented feature of iOS that one can create own
framework. This plugin closes the gap.
"""


}
