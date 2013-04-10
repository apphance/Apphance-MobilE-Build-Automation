package com.apphance.ameba.plugins.android.jarlibrary

import com.apphance.ameba.configuration.android.AndroidJarLibraryConfiguration
import com.apphance.ameba.plugins.android.jarlibrary.tasks.DeployJarLibraryTask
import com.apphance.ameba.plugins.android.jarlibrary.tasks.JarLibraryTask
import com.google.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.plugins.android.buildplugin.AndroidPlugin.READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME

/**
 * Helps building the library with resources embedded. It is useful in case we want to generate libraries like
 * *.jar that wants to have the resources embedded.
 */
class AndroidJarLibraryPlugin implements Plugin<Project> {

    public static final String DEPLOY_JAR_LIBRARY_TASK_NAME = 'deployJarLibrary'
    public static final String JAR_LIBRARY_TASK_NAME = 'jarLibrary'

    private Project project
    @Inject JarLibraryTask jarLibraryTask
    @Inject DeployJarLibraryTask deployJarLibraryTask
    @Inject AndroidJarLibraryConfiguration androidJarLibraryConfiguration

    @Override
    void apply(Project project) {
        this.project = project

        if (androidJarLibraryConfiguration.enabled) {
            prepareJarLibraryTask()
            prepareJarLibraryDeployTask()
        }
    }

    private void prepareJarLibraryTask() {
        def task = project.task(JAR_LIBRARY_TASK_NAME)
        task.description = 'Prepares jar library with embedded resources'
        task.group = AMEBA_BUILD
        task << { jarLibraryTask.jarLibrary() }
        task.dependsOn(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
    }

    private void prepareJarLibraryDeployTask() {
        def task = project.task(DEPLOY_JAR_LIBRARY_TASK_NAME)
        task.description = 'Deploys jar library to maven repository'
        task.group = AMEBA_BUILD
        project.configurations.add('jarLibraryConfiguration')
        task.doFirst(deployJarLibraryTask.deployJarLibrary)
        task.dependsOn(JAR_LIBRARY_TASK_NAME)
    }

    static public final String DESCRIPTION =
        """ |This is the plugin that makes up Android's inability to prepare standalone .jar libraries.
            |
            |Currently (android sdk v16 as of this writing) android has no features yet to provide
            |libraries as standalone .jar files. The feature is being worked on, but temporarily
            |the jarlibrary plugin provides capability of building such jar library. It embeds
            |resources of Android project (from res directory) to standard java/jar resource - with specified prefix.
            |This is not a perfect solution (for example you cannot process layouts this way - only the
            |images) but it will do for a moment. This is how Apphance service library is prepared.
            |""".stripMargin()
}
