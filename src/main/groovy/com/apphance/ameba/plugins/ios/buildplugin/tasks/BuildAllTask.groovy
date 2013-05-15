package com.apphance.ameba.plugins.ios.buildplugin.tasks

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.apphance.ameba.plugins.ios.release.IOSReleaseListener
import com.apphance.ameba.plugins.project.tasks.VerifySetupTask
import org.gradle.api.DefaultTask
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

//TODO
class BuildAllTask extends DefaultTask {

    private Project project
    @Inject
    IOSConfiguration iosConf
    @Inject
    IOSExecutor iosExecutor
    @Inject
    IOSReleaseListener releaseListener
    @Inject
    IOSSingleVariantBuilder builder

    List<String> prepareAllTasks() {
        List<String> tasks = []
        iosConf.allBuildableVariants.each { v ->
            def task = project.task("build-${v.noSpaceId}")
            task.group = AMEBA_BUILD
            task.description = "Builds target: ${v.target}, configuration: ${v.configuration}"
            task << {
                builder.registerListener(releaseListener)
                builder.buildNormalVariant(project, v.target, v.configuration)
            }
            task.dependsOn(
                    CopyMobileProvisionTask.NAME,
                    VerifySetupTask.NAME, CopySourcesTask.NAME
            )
            tasks << task.name
        }
        tasks
    }
}
