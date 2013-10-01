package com.apphance.flow.configuration.release

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.properties.URLProperty
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.validation.ReleaseValidator
import com.apphance.flow.env.Environment
import com.apphance.flow.plugins.release.FlowArtifact
import groovy.transform.PackageScope

import javax.imageio.ImageIO
import javax.inject.Inject
import java.text.SimpleDateFormat

import static com.apphance.flow.env.Environment.JENKINS
import static com.apphance.flow.util.file.FileManager.relativeTo
import static java.text.MessageFormat.format
import static java.util.ResourceBundle.getBundle
import static org.apache.commons.lang.StringUtils.isNotBlank

abstract class ReleaseConfiguration extends AbstractConfiguration {

    final String configurationName = 'Release Configuration'

    public static final String OTA_DIR = 'flow-ota'
    public static final ALL_EMAIL_FLAGS = [
            'qrCode',
            'imageMontage'
    ]

    @Inject ProjectConfiguration conf
    @Inject PropertyReader reader
    @Inject ReleaseValidator validator

    private boolean enabledInternal
    protected bundle = getBundle('validation')

    FlowArtifact otaIndexFile
    FlowArtifact fileIndexFile
    FlowArtifact plainFileIndexFile
    FlowArtifact sourcesZip
    FlowArtifact imageMontageFile
    FlowArtifact mailMessageFile
    FlowArtifact QRCodeFile

    String releaseMailSubject

    Collection<String> getReleaseNotes() {
        (reader.envVariable('RELEASE_NOTES') ?: '').split('\n').findAll {
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

    def releaseUrl = new URLProperty(
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

    URL getReleaseUrlVersioned() {
        new URL("$releaseUrl.value/$conf.fullVersionString")
    }

    @PackageScope
    String getReleaseDirName() {
        def url = releaseUrl.value
        def split = url.path.split('/')
        split[-1]
    }

    File getReleaseDir() {
        new File(otaDir, "$releaseDirName/$conf.fullVersionString")
    }

    File getOtaDir() {
        new File(conf.rootDir, OTA_DIR)
    }

    def releaseIcon = new FileProperty(
            name: 'release.icon',
            message: 'Path to project\'s icon file, must be relative to the root dir of project',
            required: { false },
            defaultValue: { defaultIcon() ? relativeTo(conf.rootDir.absolutePath, defaultIcon().absolutePath) : null },
            possibleValues: { possibleIcons() },
            validator: {
                if (!it) return true
                def file = new File(conf.rootDir, it as String)
                file?.absolutePath?.trim() ? (file.exists() && ImageIO.read(file)) : false
            }
    )

    abstract File defaultIcon()

    abstract List<String> possibleIcons()

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

    def releaseMailFrom = new StringProperty(
            name: 'release.mail.from',
            message: 'Sender email address',
            validator: { (it = it?.trim()) ? it ==~ ReleaseValidator.MAIL_PATTERN_WITH_NAME : true }
    )

    def releaseMailTo = new ListStringProperty(
            name: 'release.mail.to',
            message: 'Recipients of release email',
            validator: { it?.trim() ? it?.split(',')?.every { it?.trim() ==~ ReleaseValidator.MAIL_PATTERN_WITH_NAME } : true }
    )

    def releaseMailFlags = new ListStringProperty(
            name: 'release.mail.flags',
            message: 'Flags for release email',
            defaultValue: { ['qrCode', 'imageMontage'] as List<String> },
            validator: { it?.split(',')?.every { it?.trim() in ReleaseConfiguration.ALL_EMAIL_FLAGS } }
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

    boolean isEnabled() {
        enabledInternal && conf.isEnabled()
    }

    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    protected List<File> getFiles(File searchRootDir, String dirPattern = '.*', Closure acceptFilter) {
        List<File> icons = []
        searchRootDir?.eachDirMatch(~dirPattern) { dir ->
            icons.addAll(dir.listFiles([accept: acceptFilter] as FileFilter))
        }
        icons
    }

    @Override
    void checkProperties() {
        check !checkException { releaseDirName }, bundle.getString('exception.release.url')
        check language.validator(language.value), bundle.getString('exception.release.language')
        check country.validator(country.value), bundle.getString('exception.release.country')
        check releaseMailFrom.validator(releaseMailFrom.value), format(bundle.getString('exception.release.mail'), releaseMailFrom.name, releaseMailFrom.value)
        check releaseMailTo.validator(releaseMailTo.persistentForm()), format(bundle.getString('exception.release.mail'), releaseMailTo.name, releaseMailTo.value)
        check releaseMailFlags.validator(releaseMailFlags.persistentForm()), format(bundle.getString('exception.release.mail.flags'), ALL_EMAIL_FLAGS, releaseMailFlags.value)
        check releaseIcon.validator(releaseIcon.value), format(bundle.getString('exception.release.icon'), releaseIcon.value)

        if (Environment.env() == JENKINS) {
            check !checkException { validator.validateMailServer(mailServer) }, bundle.getString('exception.release.mail.server')
            check !checkException { validator.validateMailPort(mailPort) }, bundle.getString('exception.release.mail.port')
        }
    }
}
