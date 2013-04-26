package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.plugins.android.AndroidBuilderInfo
import org.gradle.api.Project

/**
 * Listener that can be plugged in android build. It will be fired after every variant is built.
 *
 */
interface AndroidBuildListener {

    void buildDone(Project project, AndroidBuilderInfo bi)
}
