package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.Configuration
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.google.inject.Inject

/**
 * Keeps configuration for android release.
 */
@com.google.inject.Singleton
class AndroidReleaseConfiguration extends Configuration {

    final String configurationName = 'Android release configuration'

    boolean enabled

    Map<String, AmebaArtifact> apkFiles = [:]
    Map<String, AmebaArtifact> jarFiles = [:]
    AmebaArtifact otaIndexFile
    AmebaArtifact fileIndexFile
    AmebaArtifact plainFileIndexFile

    AndroidConfiguration androidConfiguration

    @Inject
    AndroidReleaseConfiguration(AndroidConfiguration androidConfiguration) {
        this.androidConfiguration = androidConfiguration
    }

    @Override
    boolean isEnabled() {
        enabled && androidConfiguration.enabled
    }

}
