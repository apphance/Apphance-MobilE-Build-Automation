package com.apphance.ameba.plugins.android.buildplugin

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantsConfiguration
import com.apphance.ameba.executor.AndroidExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.android.buildplugin.tasks.*
import com.apphance.ameba.plugins.projectconfiguration.tasks.CleanConfTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.VERIFY_SETUP_TASK_NAME
import static org.gradle.api.logging.Logging.getLogger
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

    private l = getLogger(getClass())

    public static final String READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME = 'readAndroidProjectConfiguration'

    @Inject AndroidConfiguration androidConfiguration
    @Inject AndroidVariantsConfiguration variantsConf
    @Inject CommandExecutor executor
    @Inject AndroidExecutor androidExecutor

    @Override
    void apply(Project project) {

        if (androidConfiguration.enabled) {
            prepareJavaEnvironment(project)

            project.task(CleanClassesTask.NAME, type: CleanClassesTask)
            project.task(CopySourcesTask.name, type: CopySourcesTask)
            project.task(ReplacePackageTask.NAME, type: ReplacePackageTask)
            project.task(RunUpdateProjectTask.NAME, TYPE: RunUpdateProjectTask)

            project.task(CleanAndroidTask.NAME, type: CleanAndroidTask, dependsOn: CleanConfTask.NAME)
            project.tasks[CLEAN_TASK_NAME].dependsOn(CleanAndroidTask.NAME)

            project.task(CompileAndroidTask.NAME, type: CompileAndroidTask)
            project.tasks[JAVADOC_TASK_NAME].dependsOn(CompileAndroidTask.NAME)
            project.tasks[COMPILE_JAVA_TASK_NAME].dependsOn(CompileAndroidTask.NAME)

            variantsConf.variants.each {
                project.task("install${it.name}", type: InstallTask, variant: it,
                        dependsOn: READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)

                project.task("build${it.name}", type: SingleVariantTask, variant: it,
                        dependsOn: [READ_PROJECT_CONFIGURATION_TASK_NAME, VERIFY_SETUP_TASK_NAME, CopySourcesTask.NAME])
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
        project.dependencies.add('compile', project.files('ext-classes'))
        project.dependencies.add('compile', project.files(androidConfiguration.sdkJars))
        project.dependencies.add('compile', project.files(androidConfiguration.jarLibraries))
        project.dependencies.add('compile', project.files(androidConfiguration.linkedJarLibraries))

        println "########### project.dependencies: ${project.dependencies}"
    }
}
