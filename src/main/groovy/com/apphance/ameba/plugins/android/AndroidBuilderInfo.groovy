package com.apphance.ameba.plugins.android

import com.apphance.ameba.configuration.android.AndroidBuildMode

/**
 * Information used to build artifacts.  Useful information grouped together needed
 * by various artifacts generated along the way.
 */
class AndroidBuilderInfo {

    String fullReleaseName
    String filePrefix
    String variant
    AndroidBuildMode mode
    File originalFile
    File buildDir
    File tmpDir

    String getId() {
        variant
    }

    @Override
    String toString() {
        this.properties
    }
}
