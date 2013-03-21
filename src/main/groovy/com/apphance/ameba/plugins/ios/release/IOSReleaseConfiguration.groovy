package com.apphance.ameba.plugins.ios.release

import com.apphance.ameba.plugins.release.AmebaArtifact

/**
 * Keeps configuration of iOS project release.
 *
 */
class IOSReleaseConfiguration {

    Map<String, AmebaArtifact> distributionZipFiles = [:]
    Map<String, AmebaArtifact> dSYMZipFiles = [:]
    Map<String, AmebaArtifact> ipaFiles = [:]
    Map<String, AmebaArtifact> manifestFiles = [:]
    Map<String, AmebaArtifact> mobileProvisionFiles = [:]
    Map<String, AmebaArtifact> ahSYMDirs = [:]
    AmebaArtifact otaIndexFile
    AmebaArtifact fileIndexFile
    AmebaArtifact plainFileIndexFile
    Map<String, AmebaArtifact> dmgImageFiles = [:]

    @Override
    public String toString() {
        this.properties
    }
}
