package com.apphance.ameba.plugins.android

import com.apphance.ameba.configuration.android.AndroidBuildMode

/**
 * Information used to build artifacts.  Useful information grouped together needed
 * by various artifacts generated along the way.
 */
class AndroidBuilderInfo {

    String variant
    AndroidBuildMode mode
    File buildDirectory
    File originalFile
    String fullReleaseName
    String filePrefix
    File tmpDir

    String getId() {
        variant
    }

    @Override
    public String toString() {
        getProperties()
    }
}
