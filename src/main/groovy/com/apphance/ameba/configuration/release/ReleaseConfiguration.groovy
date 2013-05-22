package com.apphance.ameba.configuration.release

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ProjectConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.properties.URLProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.plugins.release.AmebaArtifact

import javax.imageio.ImageIO
import javax.inject.Inject
import java.text.SimpleDateFormat

import static com.apphance.ameba.util.file.FileManager.relativeTo

abstract class ReleaseConfiguration extends AbstractConfiguration {

    public static final String OTA_DIR = 'flow-ota'
    def MAIL_PATTERN = /.* *<{0,1}[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[A-Za-z]{2,4}>{0,1}/
    def ALL_EMAIL_FLAGS = [
            'installableSimulator',
            'qrCode',
            'imageMontage'
    ]

    final String configurationName = 'Release Configuration'

    private boolean enabledInternal

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

    String releaseMailSubject

    @Inject
    ProjectConfiguration conf
    @Inject
    PropertyReader reader

    Collection<String> getReleaseNotes() {
        (reader.systemProperty('release.notes') ?: reader.envVariable('RELEASE_NOTES') ?: '').split('\n')
    }

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

    String getBuildDate() {
        new SimpleDateFormat("dd-MM-yyyy HH:mm zzz", locale).format(new Date())
    }

    File getOtaDir() {
        new File(conf.rootDir, OTA_DIR)
    }

    FileProperty iconFile = new FileProperty(
            name: 'release.icon',
            message: 'Path to project\'s icon file, must be relative to the root dir of project',
            required: { true },
            defaultValue: { relativeTo(conf.rootDir.absolutePath, defaultIcon().absolutePath) },
            possibleValues: { possibleIcons() },
            validator: {
                def file = new File(conf.rootDir, it as String)
                file?.absolutePath?.trim() ? (file.exists() && ImageIO.read(file)) : false
            }
    )

    abstract File defaultIcon()

    abstract List<String> possibleIcons()

    URLProperty projectURL = new URLProperty(
            name: 'release.url',
            message: 'Base project URL where the artifacts will be placed. This should be folder URL where last element (after last /) is used as ' +
                    'subdirectory of ota dir when artifacts are created locally.',
            required: { true },
            validator: {
                try {
                    (it as String).toURL()
                    return true
                } catch (Exception e) { return false }
            }
    )

    String getProjectDirName() {
        def url = projectURL.value
        def split = url.path.split('/')
        split[-1]
    }

    URL getBaseURL() {
        def url = projectURL.value
        def split = url.path.split('/')
        new URL(url.protocol, url.host, url.port, (split[0..-2]).join('/') + '/')
    }

    def language = new StringProperty(
            name: 'release.language',
            message: 'Language of the project',
            defaultValue: { 'en' },
            validator: { it?.length() == 2 && it?.every { (it as Character).isLowerCase() } }
    )

    def country = new StringProperty(
            name: 'release.country',
            message: 'Project country',
            defaultValue: { 'US' },
            validator: { it?.length() == 2 && it?.every { (it as Character).isUpperCase() } }
    )

    StringProperty releaseMailFrom = new StringProperty(
            name: 'release.mail.from',
            message: 'Sender email address',
            validator: { (it = it?.trim()) ? it ==~ MAIL_PATTERN : true }
    )

    StringProperty releaseMailTo = new StringProperty(
            name: 'release.mail.to',
            message: 'Recipient of release email',
            validator: { (it = it?.trim()) ? it ==~ MAIL_PATTERN : true }
    )

    ListStringProperty releaseMailFlags = new ListStringProperty(
            name: 'release.mail.flags',
            message: 'Flags for release email',
            defaultValue: { ['qrCode', 'imageMontage'] as List<String> },
            validator: { it?.split(',')?.every { it?.trim() in ALL_EMAIL_FLAGS } }
    )

    String getMailPort() {
        reader.systemProperty('mail.port') ?: reader.envVariable('MAIL_PORT') ?: mailPortInternal.value ?: ''
    }

    String getMailServer() {
        reader.systemProperty('mail.server') ?: reader.envVariable('MAIL_SERVER') ?: mailServerInternal.value ?: ''
    }

    StringProperty mailPortInternal = new StringProperty(
            name: 'mail.port',
            message: 'Mail port',
            validator: { it?.matches('[0-9]+') }
    )

    StringProperty mailServerInternal = new StringProperty(
            name: 'mail.server',
            message: 'Mail server'
    )

    File getTargetDirectory() {
        new File(new File(otaDir, projectDirName), conf.fullVersionString)
    }

    URL getVersionedApplicationUrl() {
        new URL(baseURL, "${projectDirName}/${conf.fullVersionString}/")
    }

    boolean isEnabled() {
        enabledInternal && conf.isEnabled()
    }

    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }
}

