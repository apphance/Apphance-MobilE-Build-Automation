package com.apphance.ameba.plugins.ios.buildplugin

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.plugins.ios.buildplugin.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

/*
 * Plugin for various X-Code related tasks.
 * This is the main iOS build plugin.
 *
 * The plugin provides all the task needed to build iOS application.
 * Besides tasks explained below, the plugin prepares build-*
 * tasks which are dynamically created, based on targets and configurations available.
 * There is one task available per each Target-Configuration combination - unless particular
 * combination is excluded by the exclude property.
 *
 */
class IOSPlugin implements Plugin<Project> {

    @Inject
    IOSConfiguration conf

    @Override
    def void apply(Project project) {
        if (conf.isEnabled()) {
            project.task(CopySourcesTask.NAME, type: CopySourcesTask)
            project.task(CopyDebugSourcesTask.NAME, type: CopyDebugSourcesTask)
            project.task(CleanTask.NAME, type: CleanTask)
            project.task(UnlockKeyChainTask.NAME, type: UnlockKeyChainTask)
            project.task(CopyMobileProvisionTask.NAME, type: CopyMobileProvisionTask)
            project.task(BuildSingleVariantTask.NAME, type: BuildSingleVariantTask, dependsOn: [CopySourcesTask.NAME])
            project.task(IOSAllSimulatorsBuilder.NAME, type: IOSAllSimulatorsBuilder, dependsOn: [
                    CopyMobileProvisionTask.NAME,
                    CopyDebugSourcesTask.NAME])


        }
    }

    //TODO - buildAll task
//    private void prepareBuildAllTask() {
//        def task = project.task(BUILD_ALL_TASK_NAME)
//        task.group = AMEBA_BUILD
//        task.description = 'Builds all target/configuration combinations and produces all artifacts (zip, ipa, messages, etc)'
//        List<String> dependsOn = new BuildAllTask(project, executor, iosExecutor).prepareAllTasks()
//        task.dependsOn(dependsOn)
//    }
}
