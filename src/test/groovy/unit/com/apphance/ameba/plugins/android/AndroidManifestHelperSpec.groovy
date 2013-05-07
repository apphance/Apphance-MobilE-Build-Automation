package com.apphance.ameba.plugins.android

import com.google.common.io.Files
import org.gradle.api.GradleException
import spock.lang.Specification

import static com.apphance.ameba.plugins.android.AndroidManifestHelper.ANDROID_MANIFEST
import static com.google.common.io.Files.copy

class AndroidManifestHelperSpec extends Specification {

    def projectDir = new File('testProjects/android/android-basic')
    def amh = new AndroidManifestHelper()

    def 'project icon is found'() {
        expect:
        'icon' == amh.readIcon(projectDir)
    }

    def 'version is updated and read correctly'() {
        given:
        def tmpDir = Files.createTempDir()

        and:
        copy(new File(projectDir, ANDROID_MANIFEST), new File(tmpDir, ANDROID_MANIFEST))

        and:
        def versionString = '123_456_789'
        def versionCode = '3145'

        when:
        amh.updateVersion(tmpDir, versionString, versionCode)

        then:
        def versionDetails = amh.readVersion(tmpDir)
        versionDetails.versionString == versionString
        versionDetails.versionCode == versionCode

        cleanup:
        tmpDir.deleteDir()
    }

    def 'version is read correctly'() {
        given:
        def versionDetails = amh.readVersion(projectDir)

        expect:
        versionDetails.versionString == '1.0.1'
        versionDetails.versionCode == '42'
    }

    def 'package is read correctly'() {
        expect:
        amh.androidPackage(projectDir) == 'com.apphance.amebaTest.android'
    }

    def 'main activity is read correctly'() {
        expect:
        amh.getMainActivityName(new File('testProjects/apphance-updates/')) == 'pl.morizon.client.ui.HomeActivity'
    }

    def 'package and label is replaced correctly'() {
        given:
        def tmpDir = Files.createTempDir()

        and:
        def tmpManifest = new File(tmpDir, ANDROID_MANIFEST)

        and:
        copy(new File(projectDir, ANDROID_MANIFEST), tmpManifest)

        when:
        amh.replacePackage(tmpDir, 'com.apphance.amebaTest.android', newPkg, newLbl)

        then:
        amh.androidPackage(tmpDir) == newPkg
        getLabel(tmpManifest) == expectedLbl

        cleanup:
        tmpDir.deleteDir()

        where:
        newPkg                               | newLbl                 | expectedLbl
        'com.apphance.amebaTest.android.new' | null                   | '@string/app_name'
        'com.apphance.amebaTest.android.new' | '@string/app_name_new' | '@string/app_name_new'
    }

    private String getLabel(File manifest) {
        new XmlSlurper().parse(manifest).application.@'android:label'.text()
    }

    def 'exception is thrown on bad package'() {
        when:
        amh.replacePackage(projectDir, 'sample1', 'sample2')

        then:
        def e = thrown(GradleException)
        e.message == "Package to replace in manifest is: 'com.apphance.amebaTest.android' and not expected: 'sample1' (neither target: 'sample2'). This must be wrong."
    }

}
