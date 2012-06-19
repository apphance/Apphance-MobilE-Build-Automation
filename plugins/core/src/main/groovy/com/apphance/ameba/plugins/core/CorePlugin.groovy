package com.apphance.ameba.plugins.core

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer

/**
 * Plugin contains a bunch of functions and extensions,
 * widely used by Ameba.
 */
class CorePlugin implements Plugin<Project> {

    // Object extends PluginContainer class.
    // It's a class field for easier testing
    PluginContainerExtender pluginContainerExtender = new PluginContainerExtender()

    /**
     * Method extends project with some core functions
     * commonly used by other projects.
     *
     * @param project Current top-level Gradle project
     */
    @Override
    void apply(Project project) {
        pluginContainerExtender.extendPluginContainer()
    }
}
