package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.properties.URLProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.plugins.android.builder.AndroidArtifactProvider
import com.apphance.ameba.plugins.release.AmebaArtifact
import spock.lang.Specification

import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.ameba.configuration.android.AndroidBuildMode.RELEASE
import static com.google.common.io.Files.createTempDir
import static java.lang.System.getProperties
import static org.gradle.testfixtures.ProjectBuilder.builder

class AvailableArtifactsInfoTaskIntegrationSpec extends Specification {

    def rootDir = createTempDir()
    def apkDir = createTempDir()
    def tmpDir = createTempDir()

    def p = builder().withProjectDir(new File('testProjects/android/android-basic')).build()

    def projectName = 'TestAndroidProject'
    def projectUrl = "http://ota.polidea.pl/$projectName".toURL()
    def fullVersionString = '1.0.1_42'
    def mainVariant = 'MainVariant'

    def otaFolderPrefix

    def conf
    def releaseConf = new AndroidReleaseConfiguration()
    def variantsConf

    def artifactBuilder = new AndroidArtifactProvider()

    def task = p.task(AvailableArtifactsInfoTask.NAME, type: AvailableArtifactsInfoTask) as AvailableArtifactsInfoTask

    def setup() {
        properties['release.notes'] = 'release\nnotes'

        conf = GroovyMock(AndroidConfiguration)
        conf.isLibrary() >> false
        conf.fullVersionString >> fullVersionString
        conf.versionString >> '1.0.1'
        conf.projectName >> new StringProperty(value: projectName)
        conf.rootDir >> rootDir
        conf.tmpDir >> tmpDir

        releaseConf.conf = conf
        releaseConf.projectURL = new URLProperty(value: projectUrl)
        releaseConf.iconFile = new FileProperty(value: 'res/drawable-hdpi/icon.png')
        releaseConf.reader = new PropertyReader()

        variantsConf = GroovyMock(AndroidVariantsConfiguration)
        variantsConf.variants >> [
                GroovyMock(AndroidVariantConfiguration) {
                    getName() >> mainVariant
                    getMode() >> RELEASE
                },
                GroovyMock(AndroidVariantConfiguration) {
                    getName() >> 'Variant1'
                    getMode() >> DEBUG
                },
                GroovyMock(AndroidVariantConfiguration) {
                    getName() >> 'Variant2'
                    getMode() >> RELEASE
                }
        ]
        variantsConf.mainVariant >> mainVariant

        otaFolderPrefix = "${releaseConf.projectDirName}/${conf.fullVersionString}"

        artifactBuilder.conf = conf
        artifactBuilder.releaseConf = releaseConf

        task.conf = conf
        task.releaseConf = releaseConf
        task.variantsConf = variantsConf
        task.artifactBuilder = artifactBuilder
    }

    def cleanup() {
        rootDir.deleteDir()
        apkDir.deleteDir()
        tmpDir.deleteDir()
        properties.remove('release.notes')
    }

    def 'task action is executed and all artifacts are prepared'() {
        when:
        task.availableArtifactsInfo()

        then:
        def releaseDir = new File(rootDir.absolutePath, "ameba-ota/$projectName/$fullVersionString")
        releaseDir.exists()
        releaseDir.isDirectory()
        [
                'index.html',
                'icon.png',
                'file_index.html',
                'plain_file_index.html',
                "qrcode-$projectName-${fullVersionString}.png"
        ].every {
            def f = new File(releaseDir, it)
            f.exists() && f.isFile() && f.size() > 0
        }
    }

    def 'index.html is generated and validated'() {
        when:
        task.buildAPKArtifacts()
        task.prepareOTAIndexFileArtifact(otaFolderPrefix)
        task.prepareOTAIndexFile()

        then:
        def slurper = new XmlSlurper().parse(releaseConf.otaIndexFile.location)
        slurper.head.title.text() == "$projectName - Android"
        slurper.body.div[0].div[0].h1.text() == 'OTA installation - Android'
        slurper.body.div[0].div[1].div.ul.li[0].text() == projectName
        slurper.body.div[0].div[1].div.ul.li[1].text().trim().startsWith('Version: 1.0.1')
        slurper.body.div[0].div[1].div[1].section.header.h3.div.text() == 'Main installation'
        slurper.body.div[0].div[1].div[2].ul.li.div.div[0].text() == 'MainVariant'
        slurper.body.div[0].div[1].div[2].ul.li.div.div[1].text() == 'Install'
        slurper.body.div[0].div[1].div[2].ul.li.div.div[1].a.@href.text() ==
                'http://ota.polidea.pl/TestAndroidProject/1.0.1_42/TestAndroidProject-release-MainVariant-1.0.1_42.apk'
        slurper.body.div[0].div[1].p.text().contains('release')
        slurper.body.div[0].div[1].p.text().contains('notes')
        slurper.body.div[0].div[1].div[3].text() == 'Other installations'
        slurper.body.div[0].div[1].div[4].ul.li.div[0].div[0].text() == 'Variant1'
        slurper.body.div[0].div[1].div[4].ul.li.div[0].div[1].text() == 'Install'
        slurper.body.div[0].div[1].div[4].ul.li.div[0].div[1].a.@href.text() ==
                'http://ota.polidea.pl/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-Variant1-1.0.1_42.apk'
        slurper.body.div[0].div[1].div[4].ul.li.div[1].div[0].text() == 'Variant2'
        slurper.body.div[0].div[1].div[4].ul.li.div[1].div[1].text() == 'Install'
        slurper.body.div[0].div[1].div[4].ul.li.div[1].div[1].a.@href.text() ==
                'http://ota.polidea.pl/TestAndroidProject/1.0.1_42/TestAndroidProject-release-Variant2-1.0.1_42.apk'
    }

    def 'file_index.html is generated and validated'() {
        given:
        releaseConf.QRCodeFile = GroovyMock(AmebaArtifact) {
            getRelativeUrl(_) >> 'qr.png'
        }
        releaseConf.mailMessageFile = GroovyMock(AmebaArtifact) {
            getRelativeUrl(_) >> 'mail_message'
        }
        releaseConf.imageMontageFile = GroovyMock(AmebaArtifact) {
            getRelativeUrl(_) >> 'image_montage.png'
        }
        releaseConf.plainFileIndexFile = GroovyMock(AmebaArtifact) {
            getRelativeUrl(_) >> 'plain_file_index.html'
        }

        when:
        task.buildAPKArtifacts()
        task.prepareFileIndexArtifact(otaFolderPrefix)
        task.prepareFileIndexFile()

        then:
        def slurper = new XmlSlurper().parse(releaseConf.fileIndexFile.location)
        slurper.body.div[0].div[0].h1.text() == 'Files to download'
        slurper.body.div[0].div[1].div[0].section.header.h3.text() == projectName
        slurper.body.div[0].div[1].div[0].section.header.div.text().trim().startsWith('Version: 1.0.1')
        slurper.body.div[0].div[1].div[0].section.ul.li*.text().containsAll(variantsConf.variants*.name).collect {
            "APK file: $it"
        }
        slurper.body.div[0].div[1].div[0].section.ul.li.a.@href*.text().containsAll([
                'TestAndroidProject-release-MainVariant-1.0.1_42.apk',
                'TestAndroidProject-debug-Variant1-1.0.1_42.apk',
                'TestAndroidProject-release-Variant2-1.0.1_42.apk']
        )
        slurper.body.div.div.div.section.ul.li.a*.text().containsAll(
                ['Mail message', 'Image montage file', 'QR Code', 'Plain file index']
        )
        slurper.body.div.div.div.section.ul.li.a.@href*.text().containsAll(
                ['mail_message', 'image_montage.png', 'qr.png', 'plain_file_index.html']
        )
    }

    def 'plain_file_index.html is generated and validated'() {
        given:
        releaseConf.QRCodeFile = GroovyMock(AmebaArtifact) {
            getRelativeUrl(_) >> 'http://ota.polidea.pl'
            getUrl() >> 'http://ota.polidea.pl/qr.png'.toURL()
        }
        releaseConf.mailMessageFile = GroovyMock(AmebaArtifact) {
            getRelativeUrl(_) >> 'http://ota.polidea.pl'
            getUrl() >> 'http://ota.polidea.pl/mail_message'.toURL()
        }
        releaseConf.imageMontageFile = GroovyMock(AmebaArtifact) {
            getRelativeUrl(_) >> 'http://ota.polidea.pl'
            getUrl() >> 'http://ota.polidea.pl/image_montage.png'.toURL()
        }

        when:
        task.buildAPKArtifacts()
        task.preparePlainFileIndexArtifact(otaFolderPrefix)
        task.preparePlainFileIndexFile()

        then:
        def slurper = new XmlSlurper().parse(releaseConf.plainFileIndexFile.location)
        slurper.body.h1.text() == 'TestAndroidProject'
        slurper.body.text().contains('Version: 1.0.1')
        slurper.body.h2[0].text() == 'Application files'
        slurper.body.h2[1].text() == 'Other'
        slurper.body.ul.li*.text().containsAll(
                [
                        'MainVariant : http://ota.polidea.pl/TestAndroidProject/1.0.1_42/TestAndroidProject-release-MainVariant-1.0.1_42.apk',
                        'Variant1 : http://ota.polidea.pl/TestAndroidProject/1.0.1_42/TestAndroidProject-debug-Variant1-1.0.1_42.apk',
                        'Variant2 : http://ota.polidea.pl/TestAndroidProject/1.0.1_42/TestAndroidProject-release-Variant2-1.0.1_42.apk'
                ]
        )
        slurper.body.ul[1].li[0].text() == 'Mail message: http://ota.polidea.pl/mail_message'
        slurper.body.ul[1].li[1].text() == 'Image montage: http://ota.polidea.pl/image_montage.png'
        slurper.body.ul[1].li[2].text() == 'QR code: http://ota.polidea.pl/qr.png'
    }
}
