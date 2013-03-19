package com.apphance.ameba.android.plugins.jarlibrary

import com.apphance.ameba.android.plugins.jarlibrary.tasks.DeployJarLibraryTask
import com.apphance.ameba.android.plugins.jarlibrary.tasks.JarLibraryTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin.READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME

/**
 * Helps building the library with resources embedded. It is useful in case we want to generate libraries like
 * *.jar that wants to have the resources embedded.
 */
class AndroidJarLibraryPlugin implements Plugin<Project> {

    public static final String DEPLOY_JAR_LIBRARY_TASK_NAME = 'deployJarLibrary'
    public static final String JAR_LIBRARY_TASK_NAME = 'jarLibrary'

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        prepareJarLibraryTask()
        prepareJarLibraryDeployTask()

        project.prepareSetup.prepareSetupOperations << new PrepareAndroidJarLibrarySetupOperation()
        project.verifySetup.verifySetupOperations << new VerifyAndroidJarLibrarySetupOperation()
        project.showSetup.showSetupOperations << new ShowAndroidJarLibrarySetupOperation()
    }

    private void prepareJarLibraryTask() {
        def task = project.task(JAR_LIBRARY_TASK_NAME)
        task.description = 'Prepares jar library with embedded resources'
        task.group = AMEBA_BUILD
        task << { new JarLibraryTask(project).jarLibrary() }
        task.dependsOn(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)
    }

    private void prepareJarLibraryDeployTask() {
        def task = project.task(DEPLOY_JAR_LIBRARY_TASK_NAME)
        task.description = 'Deploys jar library to maven repository'
        task.group = AMEBA_BUILD
        project.configurations.add('jarLibraryConfiguration')
        task.doFirst(new DeployJarLibraryTask(project).deployJarLibrary)
        task.dependsOn(JAR_LIBRARY_TASK_NAME)
    }

    static public final String DESCRIPTION =
        """This is the plugin that makes up Android's inability to prepare standalone .jar libraries.

Currently (android sdk v16 as of this writing) android has no features yet to provide
libraries as standalone .jar files. The feature is being worked on, but temporarily
the jarlibrary plugin provides capability of building such jar library. It embeds
resources of Android project (from res directory) to standard java/jar resource - with specified prefix.
This is not a perfect solution (for example you cannot process layouts this way - only the
images) but it will do for a moment. This is how Apphance service library is prepared.
"""

}
