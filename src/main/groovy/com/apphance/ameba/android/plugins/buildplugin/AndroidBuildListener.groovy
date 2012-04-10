package com.apphance.ameba.android.plugins.buildplugin

import org.gradle.api.Project

import com.apphance.ameba.android.AndroidBuilderInfo

/**
 * Listener that can be plugged in android build. It will be fired after every variant is built.
 *
 */
interface AndroidBuildListener {
    void buildDone(Project project, AndroidBuilderInfo bi)
}
