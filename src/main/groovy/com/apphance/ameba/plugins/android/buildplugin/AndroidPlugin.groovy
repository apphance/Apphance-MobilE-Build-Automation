package com.apphance.ameba.plugins.android.buildplugin

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantsConfiguration
import com.apphance.ameba.plugins.android.buildplugin.tasks.*
import com.apphance.ameba.plugins.projectconfiguration.tasks.CleanConfTask
import com.apphance.ameba.plugins.projectconfiguration.tasks.VerifySetupTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static org.gradle.api.plugins.BasePlugin.CLEAN_TASK_NAME
import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME
import static org.gradle.api.plugins.JavaPlugin.JAVADOC_TASK_NAME

/**
 * This is the main android build plugin.
 *
 * The plugin provides all the tasks needed to build android application.
 * Besides tasks explained below, the plugin prepares build-* and install-*
 * tasks which are dynamically created, based on variants available. In
 * case the build has no variants, the only available builds are Debug and Release.
 * In case of variants, there is one build and one task created for every variant.
 */
class AndroidPlugin implements Plugin<Project> {

    public static final String BUILD_ALL_DEBUG_TASK_NAME = 'buildAllDebug'
    public static final String BUILD_ALL_RELEASE_TASK_NAME = 'buildAllRelease'
    @Inject
    private AndroidConfiguration androidConfiguration
    @Inject
    private AndroidVariantsConfiguration variantsConf

    @Override
    void apply(Project project) {

        if (androidConfiguration.isEnabled()) {
            prepareJavaEnvironment(project)

            project.task(UpdateProjectTask.NAME, type: UpdateProjectTask)

            project.task(CleanClassesTask.NAME,
                    type: CleanClassesTask,
                    dependsOn: UpdateProjectTask.NAME)

            project.task(CopySourcesTask.NAME,
                    type: CopySourcesTask,
                    dependsOn: UpdateProjectTask.NAME)

            project.task(ReplacePackageTask.NAME,
                    type: ReplacePackageTask,
                    dependsOn: UpdateProjectTask.NAME)

            project.task(CleanAndroidTask.NAME,
                    type: CleanAndroidTask,
                    dependsOn: [CleanConfTask.NAME, UpdateProjectTask.NAME])

            project.tasks[CLEAN_TASK_NAME].dependsOn(CleanAndroidTask.NAME)

            project.task(CompileAndroidTask.NAME,
                    type: CompileAndroidTask,
                    dependsOn: UpdateProjectTask.NAME)

            project.tasks[JAVADOC_TASK_NAME].dependsOn(CompileAndroidTask.NAME)
            project.tasks[COMPILE_JAVA_TASK_NAME].dependsOn(CompileAndroidTask.NAME)

            project.task(BUILD_ALL_DEBUG_TASK_NAME)
            project.task(BUILD_ALL_RELEASE_TASK_NAME)
            project.task('buildAll', dependsOn: [BUILD_ALL_DEBUG_TASK_NAME, BUILD_ALL_RELEASE_TASK_NAME])

            variantsConf.variants.each {
                def buildName = "build${it.name}"
                project.task(buildName,
                        type: SingleVariantTask,
                        dependsOn: [CopySourcesTask.NAME, UpdateProjectTask.NAME]).variant = it

                def debugRelaseBuild = "buildAll${it.mode.name().toLowerCase().capitalize()}"
                project.tasks[debugRelaseBuild].dependsOn buildName

                project.task("install${it.name}", type: InstallTask, dependsOn: buildName).variant = it
            }

            project.tasks.each {
                if (!(it.name in [VerifySetupTask.NAME, 'prepareSetup2'])) {
                    it.dependsOn VerifySetupTask.NAME
                }
            }
        }
    }

    private void prepareJavaEnvironment(Project project) {
        project.plugins.apply('java')
        def javaConventions = project.convention.plugins.java
        javaConventions.sourceSets {
            main {
                output.classesDir = project.file('bin')
                output.resourcesDir = project.file('bin')
                java { srcDir project.file('src') }
                java { srcDir project.file('gen') }
            }
        }
        project.compileJava.options.encoding = 'UTF-8'
        project.javadoc.options.encoding = 'UTF-8'
        project.compileTestJava.options.encoding = 'UTF-8'
        project.dependencies {
            add('compile', project.files('ext-classes'))
            if (androidConfiguration.sdkJars) {
                add('compile', project.files(androidConfiguration.sdkJars))
            }
            if (androidConfiguration.jarLibraries) {
                add('compile', project.files(androidConfiguration.jarLibraries))
            }
            if (androidConfiguration.linkedJarLibraries) {
                add('compile', project.files(androidConfiguration.linkedJarLibraries))
            }
        }
    }
}
