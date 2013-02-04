package com.apphance.ameba.android.plugins.release

import com.apphance.ameba.plugins.release.AmebaArtifact

/**
 * Keeps configuration for android release.
 *
 */
class AndroidReleaseConfiguration {

    Map<String, AmebaArtifact> apkFiles = [:]
    Map<String, AmebaArtifact> jarFiles = [:]
    AmebaArtifact otaIndexFile
    AmebaArtifact fileIndexFile
    AmebaArtifact plainFileIndexFile

    @Override
    public String toString() {
        this.properties
    }

}
