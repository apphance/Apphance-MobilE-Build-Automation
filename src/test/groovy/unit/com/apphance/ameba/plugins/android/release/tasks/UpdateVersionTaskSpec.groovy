package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import org.gradle.api.GradleException
import spock.lang.Specification

import static com.apphance.ameba.plugins.android.AndroidManifestHelper.ANDROID_MANIFEST
import static com.google.common.io.Files.copy
import static com.google.common.io.Files.createTempDir
import static org.gradle.testfixtures.ProjectBuilder.builder

class UpdateVersionTaskSpec extends Specification {

    def projectDir = new File('testProjects/android/android-basic')
    def p = builder().withProjectDir(projectDir).build()
    def uvt = p.task(UpdateVersionTask.NAME, type: UpdateVersionTask) as UpdateVersionTask

    def 'version code is validated correctly when empty'() {
        when:
        uvt.validateVersionCode(code)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'version.code\' has invalid value!'

        where:
        code << [null, '', '  \t', 'with letter', 'withletter', '123-123']
    }

    def 'version code is validated correctly when set'() {
        when:
        uvt.validateVersionCode(code)

        then:
        noExceptionThrown()

        where:
        code << ['121', '1']
    }

    def 'version string is validated correctly when empty'() {
        when:
        uvt.validateVersionString(code)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'version.string\' has invalid value!'

        where:
        code << [null, '  ', '  \t', 'with\tletter', 'with space']
    }

    def 'version string is validated correctly when set'() {
        when:
        uvt.validateVersionString(code)

        then:
        noExceptionThrown()

        where:
        code << ['versionString', 'version_String', 'version_String_123_4']
    }

    def 'version is updated correctly'() {
        given:
        def ac = GroovyMock(AndroidConfiguration)
        ac.externalVersionCode >> '3145'
        ac.externalVersionString >> '31.4.5'
        ac.rootDir >> projectDir

        and:
        def amh = new AndroidManifestHelper()

        and:
        uvt.conf = ac
        uvt.manifestHelper = amh

        and:
        def tmpDir = createTempDir()
        copy(new File(projectDir, ANDROID_MANIFEST), new File(tmpDir, ANDROID_MANIFEST))

        when:
        uvt.updateVersion()

        then:
        def version = amh.readVersion(projectDir)
        '3145' == version.versionCode
        '31.4.5' == version.versionString

        cleanup:
        copy(new File(tmpDir, ANDROID_MANIFEST), new File(projectDir, ANDROID_MANIFEST))
        tmpDir.deleteDir()
    }
}
