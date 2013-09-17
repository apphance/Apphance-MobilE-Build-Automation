package com.apphance.flow.plugins.ios.release.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.*
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import com.apphance.flow.plugins.ios.release.artifact.info.IOSArtifactProvider
import com.apphance.flow.plugins.release.FlowArtifact
import com.apphance.flow.util.FlowUtils
import org.gradle.api.Project
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static com.apphance.flow.configuration.release.ReleaseConfiguration.ALL_EMAIL_FLAGS
import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR
import static com.google.common.io.Files.createTempDir
import static org.gradle.testfixtures.ProjectBuilder.builder

class AvailableArtifactsInfoTaskSpec extends Specification {

    def rootDir = createTempDir()
    def tmpDir = createTempDir()

    def p = builder().withProjectDir(new File('testProjects/ios/GradleXCode')).build()

    @Shared
    def projectName = 'GradleXCode'
    def projectUrl = "http://ota.polidea.pl/$projectName".toURL()
    @Shared
    def fullVersionString = '1.0_32'

    def releaseConf = new IOSReleaseConfiguration()
    def variantsConf

    def task = p.task(AvailableArtifactsInfoTask.NAME, type: AvailableArtifactsInfoTask) as AvailableArtifactsInfoTask

    def setup() {

        def reader = GroovyStub(PropertyReader) {
            systemProperty('version.code') >> '32'
            systemProperty('version.string') >> '1.0'
            envVariable('RELEASE_NOTES') >> 'release\nnotes'
        }

        def conf = GroovySpy(IOSConfiguration) {
            getFullVersionString() >> fullVersionString
            getProjectName() >> new StringProperty(value: projectName)
            getVersionString() >> '1.0'
        }
        conf.project = GroovyStub(Project) {
            getRootDir() >> rootDir
            file(TMP_DIR) >> tmpDir
        }
        releaseConf.conf = conf
        releaseConf.releaseUrl = new URLProperty(value: projectUrl)
        releaseConf.releaseIcon = new FileProperty(value: 'icon_retina.png')
        releaseConf.releaseMailFlags = new ListStringProperty(value: ALL_EMAIL_FLAGS)
        releaseConf.manifestFiles = [
                'MainVariant': new FlowArtifact(url: "http://ota.polidea.pl/GradleXCode/MainVariant".toURL()),
                'Variant2': new FlowArtifact(url: "http://ota.polidea.pl/GradleXCode/Variant2".toURL())
        ]
        releaseConf.reader = reader

        def variants = [
                GroovyMock(AbstractIOSVariant) {
                    getTarget() >> 'GradleXCode'
                    getArchiveConfiguration() >> 'MainConf'
                    getMode() >> new IOSBuildModeProperty(value: DEVICE)
                    getMobileprovision() >> new FileProperty(value: 'sample.mobileprovision')
                    getFullVersionString() >> fullVersionString
                    getName() >> 'MainVariant'
                },
                GroovyMock(AbstractIOSVariant) {
                    getTarget() >> 'GradleXCode2'
                    getArchiveConfiguration() >> 'Conf2'
                    getMode() >> new IOSBuildModeProperty(value: DEVICE)
                    getMobileprovision() >> new FileProperty(value: 'sample2.mobileprovision')
                    getFullVersionString() >> fullVersionString
                    getName() >> 'Variant2'
                },
                GroovyMock(AbstractIOSVariant) {
                    getTarget() >> 'GradleXCode3'
                    getArchiveConfiguration() >> 'Conf3'
                    getMode() >> new IOSBuildModeProperty(value: SIMULATOR)
                    getMobileprovision() >> new FileProperty(value: 'sample3.mobileprovision')
                    getFullVersionString() >> fullVersionString
                    getName() >> 'Variant3'
                }
        ]

        variantsConf = GroovyMock(IOSVariantsConfiguration)
        variantsConf.variants >> variants
        variantsConf.mainVariant >> variants[0]
        conf.variantsConf = variantsConf

        def artifactProvider = new IOSArtifactProvider(releaseConf: releaseConf)

        task.mpParser = GroovyMock(MobileProvisionParser) {
            udids(_) >> ['a', 'b']
        }
        task.conf = conf
        task.releaseConf = releaseConf
        task.variantsConf = variantsConf
        task.artifactProvider = artifactProvider
        task.flowUtils = new FlowUtils()
    }

    def cleanup() {
        rootDir.deleteDir()
        tmpDir.deleteDir()
    }

    def 'task action is executed and all artifacts are prepared'() {
        given:
        def html = [
                'index.html',
                'file_index.html',
                'plain_file_index.html',
                'message_file.html',
        ]
        def png = [
                'icon_retina.png',
                "$projectName-$fullVersionString-qrcode.png"
        ]

        when:
        task.availableArtifactsInfo()

        then:
        def releaseDir = new File(rootDir.absolutePath, "${OTA_DIR}/$projectName/$fullVersionString")
        releaseDir.exists()
        releaseDir.isDirectory()

        (html + png).every {
            def f = new File(releaseDir, it)
            f.exists() && f.isFile() && f.size() > 0
        }
        when:
        html.every { new XmlSlurper().parse(new File(releaseDir, it)) }

        then:
        noExceptionThrown()
    }
}
