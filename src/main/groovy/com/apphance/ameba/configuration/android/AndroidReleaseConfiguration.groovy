package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.google.inject.Inject
import groovy.transform.ToString

/**
 * Keeps configuration for android release.
 */
@com.google.inject.Singleton
class AndroidReleaseConfiguration extends Configuration{

    @Inject AndroidConfiguration androidConfiguration

    boolean enabled

    Map<String, AmebaArtifact> apkFiles = [:]
    Map<String, AmebaArtifact> jarFiles = [:]
    AmebaArtifact otaIndexFile
    AmebaArtifact fileIndexFile
    AmebaArtifact plainFileIndexFile

    @Override
    boolean isEnabled() {
        enabled && androidConfiguration.enabled
    }

    final String configurationName = 'Release configuration'
}
