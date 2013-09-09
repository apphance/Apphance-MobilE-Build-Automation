package com.apphance.flow.plugins.android.release.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.properties.URLProperty
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import com.apphance.flow.util.FlowUtils
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.flow.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.flow.configuration.android.AndroidBuildMode.RELEASE
import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR
import static com.google.common.io.Files.createTempDir
import static org.gradle.testfixtures.ProjectBuilder.builder

class AvailableArtifactsInfoTaskSpec extends Specification {

    def rootDir = createTempDir()
    def apkDir = createTempDir()
    def tmpDir = createTempDir()

    def p = builder().withProjectDir(new File('testProjects/android/android-basic')).build()

    def projectName = 'TestAndroidProject'
    def projectUrl = "http://ota.polidea.pl/$projectName".toURL()
    def fullVersionString = '1.0.1_42'
    def mainVariant = 'MainVariant'

    def releaseConf = new AndroidReleaseConfiguration()
    def variantsConf

    def task = p.task(AvailableArtifactsInfoTask.NAME, type: AvailableArtifactsInfoTask) as AvailableArtifactsInfoTask

    def setup() {

        def reader = GroovyStub(PropertyReader) {
            systemProperty('version.code') >> '42'
            systemProperty('version.string') >> '1.0.1'
            envVariable('RELEASE_NOTES') >> 'release\nnotes'
        }

        def conf = GroovySpy(AndroidConfiguration) {
            isLibrary() >> false
            getFullVersionString() >> fullVersionString
            getVersionString() >> '1.0.1'
            getProjectName() >> new StringProperty(value: projectName)
        }
        conf.project = GroovyStub(Project) {
            getRootDir() >> rootDir
            file(TMP_DIR) >> tmpDir
        }
        conf.reader = reader

        releaseConf.conf = conf
        releaseConf.releaseUrl = new URLProperty(value: projectUrl)
        releaseConf.releaseIcon = new FileProperty(value: 'res/drawable-hdpi/icon.png')
        releaseConf.reader = reader

        variantsConf = GroovyMock(AndroidVariantsConfiguration)
        variantsConf.variants >> [
                GroovyMock(AndroidVariantConfiguration) {
                    getName() >> mainVariant
                    getDisplayName() >> new StringProperty(value: mainVariant)
                    getMode() >> RELEASE
                },
                GroovyMock(AndroidVariantConfiguration) {
                    getName() >> 'Variant1'
                    getDisplayName() >> new StringProperty(value: 'Variant1')
                    getMode() >> DEBUG
                },
                GroovyMock(AndroidVariantConfiguration) {
                    getName() >> 'Variant2'
                    getDisplayName() >> new StringProperty(value: 'Variant2')
                    getMode() >> RELEASE
                }
        ]
        variantsConf.mainVariant >> mainVariant

        def artifactBuilder = new AndroidArtifactProvider(conf: conf, releaseConf: releaseConf)

        task.conf = conf
        task.releaseConf = releaseConf
        task.variantsConf = variantsConf
        task.artifactBuilder = artifactBuilder
        task.flowUtils = new FlowUtils()
    }

    def cleanup() {
        rootDir.deleteDir()
        apkDir.deleteDir()
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
                'icon.png',
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
        html.every {
            new XmlSlurper().parse(new File(releaseDir, it))
        }

        then:
        noExceptionThrown()
    }
}
