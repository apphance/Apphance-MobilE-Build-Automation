package com.apphance.ameba.ios.plugins.buildplugin

import com.apphance.ameba.ios.IOSBuilderInfo
import org.gradle.api.Project

interface IOSBuildListener {
    void buildDone(Project project, IOSBuilderInfo bi)
}
