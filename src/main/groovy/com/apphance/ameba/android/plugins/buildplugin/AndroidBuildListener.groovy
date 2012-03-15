package com.apphance.ameba.android.plugins.buildplugin

import org.gradle.api.Project

import com.apphance.ameba.android.AndroidBuilderInfo

interface AndroidBuildListener {
    void buildDone(Project project, AndroidBuilderInfo bi)
}
