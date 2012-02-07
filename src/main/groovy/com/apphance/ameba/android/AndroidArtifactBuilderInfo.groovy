package com.apphance.ameba.android

import java.io.File

/**
 * Information used to build artifacts.  Useful information grouped together needed
 * by various artifacts generated along the way.
 */

class AndroidArtifactBuilderInfo  {
    String variant
    String debugRelease
    File buildDirectory
    File originalFile
    String fullReleaseName
    String folderPrefix
    String filePrefix

    String getId() {
        return variant == null ? debugRelease : variant
    }

    @Override
    public String toString() {
        return this.getProperties()
    }
}
