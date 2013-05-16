package com.apphance.ameba.plugins.android.builder

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.ameba.configuration.properties.StringProperty
import spock.lang.Specification

import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.google.common.io.Files.createTempDir

class AndroidArtifactProviderSpec extends Specification {

    def projectName = 'SampleAndroidProject'

    def ac = GroovyMock(AndroidConfiguration)
    def arc = GroovyMock(AndroidReleaseConfiguration)
    def avc = GroovyMock(AndroidVariantConfiguration)

    def tmpDir = createTempDir()
    def otaDir = createTempDir()
    def vTmpDir = createTempDir()
    def binDir

    def aab = new AndroidArtifactProvider(conf: ac, releaseConf: arc)

    def setup() {
        ac.projectName >> new StringProperty(value: projectName)
        ac.fullVersionString >> "1.0.1-42"
        ac.tmpDir >> tmpDir

        arc.otaDir >> otaDir
        arc.baseURL >> new URL("http://ota.polidea.pl/$projectName")
        arc.projectDirName >> projectName

        avc.mode >> DEBUG
        avc.name >> 'V1'
        avc.tmpDir >> vTmpDir

        binDir = new File(new File(ac.tmpDir, avc.name), 'bin')
    }

    def cleanup() {
        tmpDir.deleteDir()
        otaDir.deleteDir()
        vTmpDir.deleteDir()
        binDir.deleteDir()
    }

    def 'jar artifact builder info'() {
        when:
        def abi = aab.jarArtifactBuilderInfo(avc)

        then:
        abi.mode == DEBUG
        abi.variant == 'V1'
        abi.tmpDir == vTmpDir
        abi.buildDir == binDir
        abi.filePrefix == 'SampleAndroidProject-debug-V1-1.0.1-42'
        abi.fullReleaseName == 'SampleAndroidProject-debug-V1-1.0.1-42'
        abi.originalFile == new File(binDir, 'classes.jar')
    }

    def 'jar artifact'() {
        when:
        def ja = aab.jarArtifact(aab.jarArtifactBuilderInfo(avc))

        then:
        ja.name == 'JAR DEBUG file for V1'
        ja.url == new URL('http://ota.polidea.pl/'.toURL(), 'SampleAndroidProject/1.0.1-42/SampleAndroidProject-debug-V1-1.0.1-42.jar')
        ja.location == new File(otaDir, 'SampleAndroidProject/1.0.1-42/SampleAndroidProject-debug-V1-1.0.1-42.jar')
    }

    def 'apk artifact builder info'() {
        when:
        def abi = aab.apkArtifactBuilderInfo(avc)

        then:
        abi.mode == DEBUG
        abi.variant == 'V1'
        abi.tmpDir == vTmpDir
        abi.buildDir == new File(new File(ac.tmpDir, avc.name), 'bin')
        abi.filePrefix == 'SampleAndroidProject-debug-V1-1.0.1-42'
        abi.fullReleaseName == 'SampleAndroidProject-debug-V1-1.0.1-42'
        abi.originalFile == new File(binDir, 'SampleAndroidProject-debug.apk')
    }

    def 'apk artifact'() {
        when:
        def ja = aab.apkArtifact(aab.apkArtifactBuilderInfo(avc))

        then:
        ja.name == 'APK DEBUG file for V1'
        ja.url == new URL('http://ota.polidea.pl/'.toURL(), 'SampleAndroidProject/1.0.1-42/SampleAndroidProject-debug-V1-1.0.1-42.apk')
        ja.location == new File(otaDir, 'SampleAndroidProject/1.0.1-42/SampleAndroidProject-debug-V1-1.0.1-42.apk')
    }
}
