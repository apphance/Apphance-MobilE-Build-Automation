package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.IOSProjectConfiguration
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.apphance.ameba.plugins.ios.release.IOSReleaseListener
import org.gradle.api.Project

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.plugins.ios.buildplugin.IOSConfigurationRetriever.getIosProjectConfiguration
import static com.apphance.ameba.plugins.ios.buildplugin.IOSPlugin.*
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.VERIFY_SETUP_TASK_NAME

class BuildAllTask {

    private Project project
    private IOSProjectConfiguration iosConf
    private CommandExecutor executor
    private IOSExecutor iosExecutor

    BuildAllTask(Project project, CommandExecutor executor, IOSExecutor iosExecutor) {
        this.project = project
        this.iosConf = getIosProjectConfiguration(project)
        this.executor = executor
        this.iosExecutor = iosExecutor
    }

    List<String> prepareAllTasks() {
        List<String> tasks = []
        iosConf.allBuildableVariants.each { v ->
            def task = project.task("build-${v.noSpaceId}")
            task.group = AMEBA_BUILD
            task.description = "Builds target: ${v.target}, configuration: ${v.configuration}"
            task << {
                //TODO
                def builder = new IOSSingleVariantBuilder(project, iosExecutor, new IOSReleaseListener(project, new IOSConfiguration(* [null] * 3), new IOSReleaseConfiguration(), executor, iosExecutor))
                builder.buildNormalVariant(project, v.target, v.configuration)
            }
            task.dependsOn(
                    READ_IOS_PROJECT_CONFIGURATION_TASK_NAME,
                    COPY_MOBILE_PROVISION_TASK_NAME,
                    VERIFY_SETUP_TASK_NAME, COPY_SOURCES_TASK_NAME
            )
            tasks << task.name
        }
        tasks
    }
}
