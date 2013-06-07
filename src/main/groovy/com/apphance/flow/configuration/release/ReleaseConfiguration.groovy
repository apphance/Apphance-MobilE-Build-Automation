package com.apphance.flow.configuration.release

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.properties.URLProperty
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.env.Environment
import com.apphance.flow.plugins.release.AmebaArtifact
import org.gradle.api.GradleException

import javax.imageio.ImageIO
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import static com.apphance.flow.env.Environment.JENKINS
import static com.apphance.flow.util.file.FileManager.relativeTo
import static org.apache.commons.lang.StringUtils.isBlank
import static org.apache.commons.lang.StringUtils.isNotBlank

abstract class ReleaseConfiguration extends AbstractConfiguration {

    final String configurationName = 'Release configuration'

    public static final String OTA_DIR = 'flow-ota'
    def MAIL_PATTERN = /.* *<{0,1}[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[A-Za-z]{2,4}>{0,1}/
    def ALL_EMAIL_FLAGS = [
            'installableSimulator',
            'qrCode',
            'imageMontage'
    ]
    def static WHITESPACE = Pattern.compile('\\s+')

    @Inject ProjectConfiguration conf
    @Inject PropertyReader reader

    private boolean enabledInternal

    AmebaArtifact otaIndexFile
    AmebaArtifact fileIndexFile
    AmebaArtifact plainFileIndexFile
    AmebaArtifact sourcesZip
    AmebaArtifact imageMontageFile
    AmebaArtifact mailMessageFile
    AmebaArtifact QRCodeFile

    String releaseMailSubject

    Collection<String> getReleaseNotes() {
        (reader.systemProperty('release.notes') ?: reader.envVariable('RELEASE_NOTES') ?: '').split('\n').findAll {
            isNotBlank(it)
        }
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

    def iconFile = new FileProperty(
            name: 'release.icon',
            message: 'Path to project\'s icon file, must be relative to the root dir of project',
            required: { true },
            defaultValue: { relativeTo(conf.rootDir.absolutePath, defaultIcon().absolutePath) },
            possibleValues: { possibleIcons() },
            validator: {
                if (!it) return false
                def file = new File(conf.rootDir, it as String)
                file?.absolutePath?.trim() ? (file.exists() && ImageIO.read(file)) : false
            }
    )

    abstract File defaultIcon()

    abstract List<String> possibleIcons()

    def projectURL = new URLProperty(
            name: 'release.url',
            message: 'Base project URL where the artifacts will be placed. This should be folder URL where last element (after last /) is used as ' +
                    'subdirectory of ota dir when artifacts are created locally.',
            required: { true },
            validator: {
                try {
                    (it as String).toURL()
                    return true
                } catch (Exception e) { return false }
            },
            validationMessage: "Should be a valid URL"

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
            validator: { it ==~ /\p{Lower}{2}/ }
    )

    def country = new StringProperty(
            name: 'release.country',
            message: 'Project country',
            defaultValue: { 'US' },
            validator: { it ==~ /\p{Upper}{2}/ }
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

    File getTargetDir() {
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

    protected List<File> getFiles(File searchRootDir, String dirPattern = '.*', Closure acceptFilter) {
        List<File> icons = []
        searchRootDir.eachDirMatch(~dirPattern) { dir ->
            icons.addAll(dir.listFiles([accept: acceptFilter] as FileFilter))
        }
        icons
    }

    static void validateMailServer(String mailServer) {
        if (isBlank(mailServer) || WHITESPACE.matcher(mailServer).find())
            throw new GradleException(mailServerValidationMsg)
    }

    static void validateMailPort(String mailPort) {
        if (isBlank(mailPort) || !mailPort.matches('[0-9]+')) {
            throw new GradleException(mailPortValidationMsg)
        }
    }

    static void validateMail(StringProperty mail) {
        if (!mail.validator(mail.value)) {
            throw new GradleException(mailValidationMsg(mail))
        }
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

        if (Environment.env() == JENKINS) {
            check !checkException { validateMailServer(mailServer) }, mailServerValidationMsg
            check !checkException { validateMailPort(mailPort) }, mailPortValidationMsg
            check !checkException { validateMail(releaseMailTo) }, mailValidationMsg(releaseMailTo)
            check !checkException { validateMail(releaseMailFrom) }, mailValidationMsg(releaseMailFrom)
        }
    }

    private static mailServerValidationMsg =
        """|Property 'mail.server' has invalid value!
           |Set it either by 'mail.server' system property or
           |'MAIL_SERVER' environment variable!""".stripMargin()

    private static mailPortValidationMsg =
        """|Property 'mail.port' has invalid value!
           |Set it either by 'mail.port' system property or 'MAIL_PORT' environment variable.
           |This property must have numeric value!""".stripMargin()

    private static mailValidationMsg = {
        """|Property ${it.name} is not set!
           |It should be valid email address!""".stripMargin()
    }
}

