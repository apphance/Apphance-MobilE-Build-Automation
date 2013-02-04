package com.apphance.ameba

import org.gradle.api.GradleException
import org.gradle.api.Project

class PluginHelper {

    public static void checkAnyPluginIsLoaded(Project project, def currentPlugin, def ... pluginClasses) {
        boolean anyPluginLoaded = false
        pluginClasses.each {
            if (project.plugins.collect { it.class }.contains(it)) {
                anyPluginLoaded = true
            }
        }
        if (!anyPluginLoaded) {
            throw new GradleException("None of the plugins ${pluginClasses} has been loaded yet. Please make sure one of them is put before ${currentPlugin}")
        }
    }

    public static void checkAllPluginsAreLoaded(Project project, def currentPlugin, def ... pluginClasses) {

        pluginClasses.each {
            if (!project.plugins.collect { it.class }.contains(it)) {
                throw new GradleException("The plugin ${it} has not been loaded yet. Please make sure you put it before ${currentPlugin}")
            }
        }
    }
}
