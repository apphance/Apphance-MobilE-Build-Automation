package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.google.inject.Inject

/**
 * Keeps configuration for android release.
 */
@com.google.inject.Singleton
class AndroidReleaseConfiguration extends AbstractConfiguration {

    final String configurationName = 'Android release configuration'

    private boolean enabled

    Map<String, AmebaArtifact> apkFiles = [:]

    Map<String, AmebaArtifact> jarFiles = [:]
    AmebaArtifact otaIndexFile
    AmebaArtifact fileIndexFile
    AmebaArtifact plainFileIndexFile
    AmebaArtifact sourcesZip

    AmebaArtifact documentationZip
    AmebaArtifact imageMontageFile
    AmebaArtifact mailMessageFile
    AmebaArtifact qrCodeFile
    AmebaArtifact galleryCss

    AmebaArtifact galleryJs
    AmebaArtifact galleryTrans
    Collection<String> releaseNotes

    String projectDirectoryName
    File otaDirectory
    String buildDate
    Collection<String> releaseMailFlags
    String releaseMailSubject
    Locale locale

    private AndroidConfiguration androidConfiguration

    @Inject
    AndroidReleaseConfiguration(AndroidConfiguration androidConfiguration) {
        this.androidConfiguration = androidConfiguration
    }

    def projectIconFile = new FileProperty(
            name: 'android.release.project.icon.file',
            message: 'Path to project\'s icon file'
    )

    def projectUrl = new StringProperty(
            name: 'android.release.project.url',
            message: 'Base project URL where the artifacts will be placed. This should be folder URL where last element (after last /) is used as ' +
                    'subdirectory of ota dir when artifacts are created locally.'
    )

    def projectLanguage = new StringProperty(
            name: 'android.release.project.language',
            message: 'Language of the project',
            defaultValue: { 'en' }
    )

    def projectCountry = new StringProperty(
            name: 'android.release.project.country',
            message: 'Project country',
            defaultValue: { 'US' }
    )

    def mailFrom = new StringProperty(
            name: 'android.release.mail.from',
            message: 'Sender email address'
    )

    def mailTo = new StringProperty(
            name: 'android.release.mail.to',
            message: 'Recipient of release email'
    )

    def mailFlags = new StringProperty(
            name: 'android.release.mail.flags',
            message: 'Flags for release email',
            defaultValue: { 'qrCode,imageMontage' }
    )

    File getTargetDirectory() {
        new File(new File(otaDirectory, projectDirectoryName), androidConfiguration.versionString.value)
    }

    @Override
    boolean isEnabled() {
        this.@enabled
    }

    @Override
    void setEnabled(boolean enabled) {
        this.@enabled = enabled
    }

    @Override
    boolean isActive() {
        this.@enabled && androidConfiguration.active
    }
}
