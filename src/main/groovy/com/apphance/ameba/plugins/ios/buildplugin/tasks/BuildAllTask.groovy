package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.apphance.ameba.plugins.ios.release.IOSReleaseListener
import org.gradle.api.DefaultTask
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.VERIFY_SETUP_TASK_NAME

//TODO
class BuildAllTask extends DefaultTask {

    private Project project
    @Inject
    IOSConfiguration iosConf
    @Inject
    CommandExecutor executor
    @Inject
    IOSExecutor iosExecutor

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
                    CopyMobileProvisionTask.NAME,
                    VERIFY_SETUP_TASK_NAME, CopySourcesTask.NAME
            )
            tasks << task.name
        }
        tasks
    }
}
