package com.apphance.ameba.android
/**
 * Information used to build artifacts.  Useful information grouped together needed
 * by various artifacts generated along the way.
 */
class AndroidBuilderInfo {

    String variant
    String debugRelease
    File buildDirectory
    File originalFile
    String fullReleaseName
    String filePrefix

    String getId() {
        variant
    }

    @Override
    public String toString() {
        getProperties()
    }
}
