package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ReleaseConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.properties.URLProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.plugins.release.AmebaArtifact

import javax.inject.Inject
import java.text.SimpleDateFormat

@com.google.inject.Singleton
class IOSReleaseConfiguration extends AbstractConfiguration implements ReleaseConfiguration {

    String configurationName = 'iOS Release Configuration'
    private boolean enabledInternal = false

    @Inject
    IOSConfiguration conf

    Map<String, AmebaArtifact> distributionZipFiles = [:]
    Map<String, AmebaArtifact> dSYMZipFiles = [:]
    Map<String, AmebaArtifact> ipaFiles = [:]
    Map<String, AmebaArtifact> manifestFiles = [:]
    Map<String, AmebaArtifact> mobileProvisionFiles = [:]
    Map<String, AmebaArtifact> ahSYMDirs = [:]
    Map<String, AmebaArtifact> dmgImageFiles = [:]

    AmebaArtifact otaIndexFile
    AmebaArtifact fileIndexFile
    AmebaArtifact plainFileIndexFile
    AmebaArtifact sourcesZip
    AmebaArtifact imageMontageFile
    AmebaArtifact mailMessageFile
    AmebaArtifact QRCodeFile
    AmebaArtifact galleryCSS
    AmebaArtifact galleryJS
    AmebaArtifact galleryTrans

    @Inject
    PropertyReader reader

    @Override
    Collection<String> getReleaseNotes() {
        (reader.systemProperty('release.notes') ?: reader.envVariable('RELEASE_NOTES') ?: '').split('\n')
    }

    URLProperty projectURL = new URLProperty(
            name: 'ios.release.project.url',
            message: 'Base project URL where the artifacts will be placed. This should be folder URL where last element (after last /) is used as ' +
                    'subdirectory of ota dir when artifacts are created locally.',
            required: { true },
            validator: {
                try {
                    projectURL.value
                    return true
                } catch (Exception e) { return false }
            }
    )

    @Override
    String getProjectDirName() {
        def url = projectURL.value
        def split = url.path.split('/')
        split[-1]
    }

    @Override
    URL getBaseURL() {
        def url = projectURL.value
        def split = url.path.split('/')
        new URL(url.protocol, url.host, url.port, (split[0..-2]).join('/') + '/')
    }

    @Override
    File getOtaDir() {
        new File(conf.rootDir, 'ameba-ota')
    }

    @Override
    String getBuildDate() {
        new SimpleDateFormat('dd-MM-yyyy HH:mm zzz', locale).format(new Date())
    }

    ListStringProperty releaseMailFlags = new ListStringProperty(
            name: 'ios.release.mail.flags',
            message: 'Flags for release email',
            defaultValue: { ['qrCode', 'imageMontage'] as List<String> },
            validator: { it?.split(',')?.every { it?.trim() in ALL_EMAIL_FLAGS } }
    )

    String releaseMailSubject

    StringProperty releaseMailFrom = new StringProperty(
            name: 'ios.release.mail.from',
            message: 'Sender email address',
            validator: { (it = it?.trim()) ? it ==~ MAIL_PATTERN : true }
    )

    StringProperty releaseMailTo = new StringProperty(
            name: 'ios.release.mail.to',
            message: 'Recipient of release email',
            validator: { (it = it?.trim()) ? it ==~ MAIL_PATTERN : true }
    )

    @Override
    FileProperty getIconFile() {
        //TODO
        return null
    }

    @Override
    Locale getLocale() {
        def lang = language.value?.trim()
        def country = country.value?.trim()

        def locale = Locale.getDefault()

        if (lang && country)
            locale = new Locale(lang, country)
        else if (lang)
            locale = new Locale(lang)

        locale
    }

    def language = new StringProperty(
            name: 'ios.release.project.language',
            message: 'Language of the project',
            defaultValue: { 'en' },
            validator: { it?.length() == 2 && it?.every { (it as Character).isLowerCase() } }
    )

    def country = new StringProperty(
            name: 'ios.release.project.country',
            message: 'Project country',
            defaultValue: { 'US' },
            validator: { it?.length() == 2 && it?.every { (it as Character).isUpperCase() } }
    )

    @Override
    File getTargetDirectory() {
        new File(new File(otaDir, projectDirName), conf.fullVersionString)
    }

    @Override
    URL getVersionedApplicationUrl() {
        new URL(baseURL, "${projectDirName}/${conf.fullVersionString}/")
    }

    @Override
    String getMailPort() {
        reader.systemProperty('mail.port') ?: reader.envVariable('MAIL_PORT') ?: mailPortInternal.value ?: ''
    }

    @Override
    String getMailServer() {
        reader.systemProperty('mail.server') ?: reader.envVariable('MAIL_SERVER') ?: mailServerInternal.value ?: ''
    }

    private StringProperty mailPortInternal = new StringProperty(
            name: 'mail.port',
            message: 'Mail port',
            validator: { it?.matches('[0-9]+') }
    )

    private StringProperty mailServerInternal = new StringProperty(
            name: 'mail.server',
            message: 'Mail server'
    )

    @Override
    boolean isEnabled() {
        conf.enabled && enabledInternal
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }
}
