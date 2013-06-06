package com.apphance.ameba.plugins.android.builder

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.ameba.configuration.properties.FileProperty
import com.apphance.ameba.configuration.properties.StringProperty
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.ameba.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.google.common.io.Files.createTempDir

class AndroidArtifactProviderSpec extends Specification {

    def projectName = 'SampleAndroidProject'

    def ac = GroovySpy(AndroidConfiguration) {
        getVersionString() >> "1.0.1"
        getVersionCode() >> '42'
        getProjectName() >> new StringProperty(value: projectName)
    }
    def arc = GroovyStub(AndroidReleaseConfiguration)
    def avc = GroovyStub(AndroidVariantConfiguration)

    def tmpDir = createTempDir()

    def project = GroovyStub(Project) {
        file(TMP_DIR) >> tmpDir
    }
    def otaDir = createTempDir()
    def vTmpDir = createTempDir()
    def variantDir = createTempDir()
    def binDir

    def aab = new AndroidArtifactProvider(conf: ac, releaseConf: arc)

    def setup() {

        ac.project = project

        arc.otaDir >> otaDir
        arc.baseURL >> new URL("http://ota.polidea.pl/$projectName")
        arc.projectDirName >> projectName

        avc.mode >> DEBUG
        avc.name >> 'V1'
        avc.tmpDir >> vTmpDir
        avc.variantDir >> new FileProperty(value: variantDir)

        binDir = new File(new File(ac.tmpDir, avc.name), 'bin')
    }

    def cleanup() {
        tmpDir.deleteDir()
        otaDir.deleteDir()
        vTmpDir.deleteDir()
        binDir.deleteDir()
        variantDir.deleteDir()
    }

    def 'jar artifact builder info'() {
        when:
        aab.conf.isLibrary() >> true
        def abi = aab.builderInfo(avc)

        then:
        abi.mode == DEBUG
        abi.variant == 'V1'
        abi.tmpDir == vTmpDir
        abi.buildDir == binDir
        abi.variantDir == variantDir
        abi.filePrefix == 'SampleAndroidProject-debug-V1-1.0.1_42'
        abi.fullReleaseName == 'SampleAndroidProject-debug-V1-1.0.1_42'
        abi.originalFile == new File(binDir, 'classes.jar')
    }

    def 'jar artifact'() {
        when:
        aab.conf.isLibrary() >> true
        def ja = aab.artifact(aab.builderInfo(avc))

        then:
        ja.name == 'JAR DEBUG file for V1'
        ja.url == new URL('http://ota.polidea.pl/'.toURL(), 'SampleAndroidProject/1.0.1_42/SampleAndroidProject-debug-V1-1.0.1_42.jar')
        ja.location == new File(otaDir, 'SampleAndroidProject/1.0.1_42/SampleAndroidProject-debug-V1-1.0.1_42.jar')
    }

    def 'apk artifact builder info'() {
        when:
        aab.conf.isLibrary() >> false
        def abi = aab.builderInfo(avc)

        then:
        abi.mode == DEBUG
        abi.variant == 'V1'
        abi.tmpDir == vTmpDir
        abi.buildDir == new File(new File(ac.tmpDir, avc.name), 'bin')
        abi.variantDir == variantDir
        abi.filePrefix == 'SampleAndroidProject-debug-V1-1.0.1_42'
        abi.fullReleaseName == 'SampleAndroidProject-debug-V1-1.0.1_42'
        abi.originalFile == new File(binDir, 'SampleAndroidProject-debug.apk')
    }

    def 'apk artifact'() {
        when:
        aab.conf.isLibrary() >> false
        def ja = aab.artifact(aab.builderInfo(avc))

        then:
        ja.name == 'APK DEBUG file for V1'
        ja.url == new URL('http://ota.polidea.pl/'.toURL(), 'SampleAndroidProject/1.0.1_42/SampleAndroidProject-debug-V1-1.0.1_42.apk')
        ja.location == new File(otaDir, 'SampleAndroidProject/1.0.1_42/SampleAndroidProject-debug-V1-1.0.1_42.apk')
    }
}
