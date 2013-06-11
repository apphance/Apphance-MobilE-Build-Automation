package com.apphance.flow.plugins.ios.buildplugin

import com.apphance.flow.plugins.ios.builder.IOSBuilderInfo

/**
 * Listens for finishing particular builds. You can register listeners to receive notifications
 * after builds are finished (one per each variant build).
 *
 */
interface IOSBuildListener {
    void buildDone(IOSBuilderInfo bi)
}
