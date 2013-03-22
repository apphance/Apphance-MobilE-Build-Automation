package com.apphance.ameba.plugins.ios.buildplugin

import com.apphance.ameba.plugins.ios.IOSBuilderInfo
import org.gradle.api.Project

/**
 * Listens for finishing particular builds. You can register listeners to receive notifications
 * after builds are finished (one per each variant build).
 *
 */
interface IOSBuildListener {
    void buildDone(Project project, IOSBuilderInfo bi)
}
