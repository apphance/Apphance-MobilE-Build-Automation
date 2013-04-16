package com.apphance.ameba.plugins.android.jarlibrary

import com.apphance.ameba.configuration.android.AndroidJarLibraryConfiguration
import com.apphance.ameba.plugins.android.jarlibrary.tasks.DeployJarLibraryTask
import com.apphance.ameba.plugins.android.jarlibrary.tasks.JarLibraryTask
import com.google.inject.Inject
import org.gradle.api.Plugin
import org.gradle.api.Project

import static com.apphance.ameba.plugins.android.buildplugin.AndroidPlugin.READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME

/**
 * Helps building the library with resources embedded. It is useful in case we want to generate libraries like
 * *.jar that wants to have the resources embedded.
 *
 * This is the plugin that makes up Android's inability to prepare standalone .jar libraries.
 *
 * Currently (android sdk v16 as of this writing) android has no features yet to provide
 * libraries as standalone .jar files. The feature is being worked on, but temporarily
 * the jar library plugin provides capability of building such jar library. It embeds
 * resources of Android project (from res directory) to standard java/jar resource - with specified prefix.
 * This is not a perfect solution (for example you cannot process layouts this way - only the
 * images) but it will do for a moment. This is how Apphance service library is prepared.
 */
class AndroidJarLibraryPlugin implements Plugin<Project> {

    @Inject
    private AndroidJarLibraryConfiguration jarLibConf

    @Override
    void apply(Project project) {
        if (jarLibConf.isEnabled()) {

            project.task(JarLibraryTask.NAME,
                    type: JarLibraryTask,
                    dependsOn: READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)

            project.configurations.add('jarLibraryConfiguration')

            project.task(DeployJarLibraryTask.NAME,
                    type: DeployJarLibraryTask,
                    dependsOn: JarLibraryTask.NAME)
        }
    }
}
