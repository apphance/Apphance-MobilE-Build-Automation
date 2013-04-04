package com.apphance.ameba.configuration.android

import com.apphance.ameba.plugins.release.AmebaArtifact
import groovy.transform.ToString

/**
 * Keeps configuration for android release.
 */
@ToString
@com.google.inject.Singleton
class AndroidReleaseConfiguration {

    Map<String, AmebaArtifact> apkFiles = [:]
    Map<String, AmebaArtifact> jarFiles = [:]
    AmebaArtifact otaIndexFile
    AmebaArtifact fileIndexFile
    AmebaArtifact plainFileIndexFile
}
