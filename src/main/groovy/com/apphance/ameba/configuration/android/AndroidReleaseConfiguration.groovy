package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ReleaseConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.properties.URLProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import com.apphance.ameba.plugins.release.AmebaArtifact

import javax.imageio.ImageIO
import javax.inject.Inject
import java.text.SimpleDateFormat

/**
 * Keeps configuration for android release.
 */
@com.google.inject.Singleton
class AndroidReleaseConfiguration extends AbstractConfiguration implements ReleaseConfiguration {

    final String configurationName = 'Android Release Configuration'

    static final MAIL_PATTERN = /.* *<{0,1}[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[A-Za-z]{2,4}>{0,1}/
    static final ICON_PATTERN = /icon.*\.(png|jpg|jpeg|bmp)/
    public final DRAWABLE_DIR = /(drawable-ldpi|drawable-mdpi|drawable-hdpi|drawable-xhdpi|drawable)/

    static final ALL_EMAIL_FLAGS = [
            'installableSimulator',
            'qrCode',
            'imageMontage'
    ]
    private boolean enabledInternal

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
    String releaseMailSubject
    @Inject
    AndroidConfiguration conf

    @Inject
    AndroidManifestHelper manifestHelper

    @Inject
    PropertyReader reader

    Collection<String> getReleaseNotes() {
        (reader.systemProperty('release.notes') ?: reader.envVariable('RELEASE_NOTES') ?: '').split('\n')
    }

    @Override
    String getReleaseCode() {
        reader.systemProperty('release.code') ?: reader.envVariable('RELEASE_CODE') ?: ''
    }

    @Override
    String getReleaseString() {
        reader.systemProperty('release.string') ?: reader.envVariable('RELEASE_STRING') ?: ''
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

    @Override
    String getBuildDate() {
        new SimpleDateFormat("dd-MM-yyyy HH:mm zzz", locale).format(new Date())
    }

    @Override
    File getOtaDir() {
        new File(conf.rootDir, 'ameba-ota')
    }

    FileProperty iconFile = new FileProperty(
            name: 'android.release.project.icon.file',
            message: 'Path to project\'s icon file, must be relative to the root dir of project',
            required: { true },
            defaultValue: { defaultIcon() },
            possibleValues: { possibleIcons() },
            validator: {
                def file = new File(conf.rootDir, it as String)
                file?.absolutePath?.trim() ? (file.exists() && ImageIO.read(file)) : false
            }
    )

    File defaultIcon() {
        def icon = manifestHelper.readIcon(conf.rootDir)?.trim()
        def icons = []
        if (icon) {
            conf.resDir.eachDirMatch(~DRAWABLE_DIR) { dir ->
                icons.addAll(dir.listFiles([accept: { it.name.startsWith(icon) }] as FileFilter)*.canonicalFile)
            }
        }
        icons.size() > 0 ? icons.sort()[1] as File : null
    }

    @groovy.transform.PackageScope
    List<String> possibleIcons() {
        def icons = []
        conf.resDir.eachDirMatch(~DRAWABLE_DIR) { dir ->
            icons.addAll(dir.listFiles([accept: { (it.name =~ ICON_PATTERN).matches() }] as FileFilter)*.canonicalPath)
        }
        icons.collect { it.replaceAll("${conf.rootDir.absolutePath}/", '') }
    }

    URLProperty projectURL = new URLProperty(
            name: 'android.release.project.url',
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

    def language = new StringProperty(
            name: 'android.release.project.language',
            message: 'Language of the project',
            defaultValue: { 'en' },
            validator: { it?.length() == 2 && it?.every { (it as Character).isLowerCase() } }
    )

    def country = new StringProperty(
            name: 'android.release.project.country',
            message: 'Project country',
            defaultValue: { 'US' },
            validator: { it?.length() == 2 && it?.every { (it as Character).isUpperCase() } }
    )

    StringProperty releaseMailFrom = new StringProperty(
            name: 'android.release.mail.from',
            message: 'Sender email address',
            validator: { (it = it?.trim()) ? it ==~ MAIL_PATTERN : true }
    )

    StringProperty releaseMailTo = new StringProperty(
            name: 'android.release.mail.to',
            message: 'Recipient of release email',
            validator: { (it = it?.trim()) ? it ==~ MAIL_PATTERN : true }
    )

    ListStringProperty releaseMailFlags = new ListStringProperty(
            name: 'android.release.mail.flags',
            message: 'Flags for release email',
            defaultValue: { ['qrCode', 'imageMontage'] as List<String> },
            validator: { it?.split(',')?.every { it?.trim() in ALL_EMAIL_FLAGS } }
    )

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
    String getMailPort() {
        reader.systemProperty('mail.port') ?: reader.envVariable('MAIL_PORT') ?: mailPortInternal.value ?: ''
    }

    @Override
    String getMailServer() {
        reader.systemProperty('mail.server') ?: reader.envVariable('MAIL_SERVER') ?: mailServerInternal.value ?: ''
    }

    @Override
    File getTargetDirectory() {
        new File(new File(otaDir, projectDirName), conf.fullVersionString)
    }

    @Override
    URL getVersionedApplicationUrl() {
        new URL(baseURL, "${projectDirName}/${conf.fullVersionString}/")
    }

    @Override
    boolean isEnabled() {
        enabledInternal && conf.isEnabled()
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    @Override
    void checkProperties() {
        check !checkException { baseURL }, "Property '${projectURL.name}' is not valid! Should be valid URL address!"
        check language.validator(language.value), "Property '${language.name}' is not valid! Should be two letter lowercase!"
        check country.validator(country.value), "Property '${country.name}' is not valid! Should be two letter uppercase!"
        check releaseMailFrom.validator(releaseMailFrom.value), "Property '${releaseMailFrom.name}' is not valid! Should be valid " +
                "email address! Current value: ${releaseMailFrom.value}"
        check releaseMailTo.validator(releaseMailTo.value), "Property '${releaseMailTo.name}' is not valid! Should be valid email address!  Current value: ${releaseMailTo.value}"
        check releaseMailFlags.validator(releaseMailFlags.persistentForm()), "Property '${releaseMailFlags.name}' is not valid! Possible values: " +
                "${ALL_EMAIL_FLAGS} Current value: ${releaseMailFlags.value}"
        check iconFile.validator(iconFile.value), "Property '${iconFile.name}' (${iconFile.value}) is not valid! Should be existing image file!"
    }
}