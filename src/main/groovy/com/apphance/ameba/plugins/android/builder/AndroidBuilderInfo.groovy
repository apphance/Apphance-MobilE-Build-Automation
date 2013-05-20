package com.apphance.ameba.plugins.android.builder

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
    File variantDir

    String getId() {
        variant
    }

    @Override
    String toString() {
        this.properties
    }
}
