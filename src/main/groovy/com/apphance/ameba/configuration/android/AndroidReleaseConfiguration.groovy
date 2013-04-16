package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ReleaseConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.properties.URLProperty
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.google.inject.Inject

/**
 * Keeps configuration for android release.
 */
@com.google.inject.Singleton
class AndroidReleaseConfiguration extends AbstractConfiguration implements ReleaseConfiguration {

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
    AmebaArtifact QRCodeFile

    AmebaArtifact galleryCSS
    AmebaArtifact galleryJS
    AmebaArtifact galleryTrans

    Collection<String> releaseNotes

    String projectDirectoryName
    File otaDirectory
    String buildDate
    String releaseMailSubject
    Locale locale

    private AndroidConfiguration androidConfiguration

    @Inject
    AndroidReleaseConfiguration(AndroidConfiguration androidConfiguration) {
        this.androidConfiguration = androidConfiguration
    }

    FileProperty projectIconFile = new FileProperty(
            name: 'android.release.project.icon.file',
            message: 'Path to project\'s icon file'
    )

    URLProperty projectURL = new URLProperty(
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

    StringProperty releaseMailFrom = new StringProperty(
            name: 'android.release.mail.from',
            message: 'Sender email address'
    )

    StringProperty releaseMailTo = new StringProperty(
            name: 'android.release.mail.to',
            message: 'Recipient of release email'
    )

    ListStringProperty releaseMailFlags = new ListStringProperty(
            name: 'android.release.mail.flags',
            message: 'Flags for release email',
            defaultValue: { ['qrCode', 'imageMontage'] as List<String> }
    )

    File getTargetDirectory() {
        new File(new File(otaDirectory, projectDirectoryName), androidConfiguration.versionString.value)
    }

    @Override
    URL getVersionedApplicationUrl() {
        //TODO
        return null  //To change body of implemented methods use File | Settings | File Templates.
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
