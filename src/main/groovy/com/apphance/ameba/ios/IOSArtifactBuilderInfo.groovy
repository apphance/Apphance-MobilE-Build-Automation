package com.apphance.ameba.ios;

import java.io.File

/**
 * Information for single artict being built. Useful information grouped together needed
 * by various artifacts generated along the way.
 */
class IOSArtifactBuilderInfo {
    String id
    String target
    String configuration
    File buildDirectory
    String fullReleaseName
    String folderPrefix
    String filePrefix
    File mobileprovisionFile
    File plistFile

    @Override
    public String toString() {
        return this.getProperties()
    }
}
