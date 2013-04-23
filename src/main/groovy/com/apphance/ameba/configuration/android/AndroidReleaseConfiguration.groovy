package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ReleaseConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.properties.URLProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.plugins.release.AmebaArtifact
import com.google.inject.Inject

import java.text.SimpleDateFormat

/**
 * Keeps configuration for android release.
 */
@com.google.inject.Singleton
class AndroidReleaseConfiguration extends AbstractConfiguration implements ReleaseConfiguration {

    final String configurationName = 'Android Release Configuration'

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
    private AndroidConfiguration androidConfiguration
    private PropertyReader reader

    @Inject
    AndroidReleaseConfiguration(AndroidConfiguration androidConfiguration, PropertyReader reader) {
        this.androidConfiguration = androidConfiguration
        this.reader = reader
    }

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
        def lang = projectLanguage.value?.trim()
        def country = projectCountry.value?.trim()

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
        new File(androidConfiguration.rootDir, 'ameba-ota')
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

    private StringProperty mailPortInternal = new StringProperty(
            name: 'mail.port',
            message: 'Mail port'
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
        new File(new File(otaDir, projectDirName), androidConfiguration.fullVersionString)
    }

    @Override
    URL getVersionedApplicationUrl() {
        new URL(baseURL, "${projectDirName}/${androidConfiguration.fullVersionString}/")
    }

    @Override
    boolean isEnabled() {
        enabledInternal && androidConfiguration.isEnabled()
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    @Override
    void checkProperties() {
        check !checkExecption { baseURL }, "Error in android.release.project.url property: ${checkExecption { baseURL }}"
        check projectIconFile.value, "property ${projectIconFile.name} missing"
    }
}

//TODO verify - copied from VerifyReleaseSetupOperation

//public static final def ALL_EMAIL_FLAGS = [
//        'installableSimulator',
//        'qrCode',
//        'imageMontage'
//]
//VerifyReleaseSetupOperation() {
//    super(ProjectReleaseProperty.class)
//}
//
//@Override
//void verifySetup() {
//    def projectProperties = readProperties()
//
//    ProjectReleaseProperty.findAll { it.defaultValue == null && it != ProjectReleaseProperty.RELEASE_PROJECT_ICON_FILE }.each {
//        checkProperty(projectProperties, it)
//    }
//
//    checkReleaseMailFlags()
//    checkIconFile()
//    checkUrl()
//    checkLanguage()
//    checkCountry()
//    checkEmail(ProjectReleaseProperty.RELEASE_MAIL_FROM)
//    checkEmail(ProjectReleaseProperty.RELEASE_MAIL_TO)
//    allPropertiesOK()
//}
//
//void checkReleaseMailFlags() {
//    use(PropertyCategory) {
//        String flags = project.readProperty(ProjectReleaseProperty.RELEASE_MAIL_FLAGS)
//        if (flags != null) {
//            flags.split(',').each {
//                if (!(it in ALL_EMAIL_FLAGS)) {
//                    throw new GradleException("The flag in ${ProjectReleaseProperty.RELEASE_MAIL_FLAGS.propertyName}: ${it} is not one of  ${ALL_EMAIL_FLAGS}")
//                }
//            }
//        }
//    }
//}
//
//void checkEmail(property) {
//    use(PropertyCategory) {
//        String email = project.readProperty(property)
//        if (!(email ==~ /.* *<{0,1}[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[A-Za-z]{2,4}>{0,1}/)) {
//            throw new GradleException("The email in ${property.propertyName}: ${email} is not valid")
//        }
//    }
//}
//
//void checkIconFile() {
//    use(PropertyCategory) {
//        String iconPath = project.readProperty(ProjectReleaseProperty.RELEASE_PROJECT_ICON_FILE)
//        if (iconPath != null && !iconPath.empty) {
//            File iconFile = project.file(iconPath)
//            if (!iconFile.exists() || !iconFile.isFile()) {
//                throw new GradleException("""The icon file property ${ProjectReleaseProperty.RELEASE_PROJECT_ICON_FILE.propertyName}: ${iconFile}) does not exist
//        or is not a file. Please run 'gradle prepareSetup' to correct it.""")
//            }
//        }
//    }
//}
//
//private checkUrl() {
//    use(PropertyCategory) {
//        String urlString = project.readProperty(ProjectReleaseProperty.RELEASE_PROJECT_URL)
//        try {
//            new URL(urlString)
//        } catch (MalformedURLException e) {
//            throw new GradleException("The ${ProjectReleaseProperty.RELEASE_PROJECT_URL.propertyName}:${urlString} property is not a valid URL: ${e}")
//        }
//    }
//}
//
//private checkLanguage() {
//    use(PropertyCategory) {
//        String language = project.readProperty(ProjectReleaseProperty.RELEASE_PROJECT_LANGUAGE)
//        if (language.length() != 2 || language.toLowerCase() != language) {
//            throw new GradleException("The ${ProjectReleaseProperty.RELEASE_PROJECT_LANGUAGE.propertyName}: ${language} property is not a valid language: should be 2 letter lowercase")
//        }
//    }
//}
//
//private checkCountry() {
//    use(PropertyCategory) {
//        String country = project.readProperty(ProjectReleaseProperty.RELEASE_PROJECT_COUNTRY)
//        if (country.length() != 2 || country.toUpperCase() != country) {
//            throw new GradleException("The ${ProjectReleaseProperty.RELEASE_PROJECT_COUNTRY.propertyName}: ${country} property is not a valid country: should be 2 letter UPPERCASE")
//        }
//    }
//}
