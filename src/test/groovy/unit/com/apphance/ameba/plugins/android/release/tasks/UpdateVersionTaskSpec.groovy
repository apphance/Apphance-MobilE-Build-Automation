package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
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

    def 'release code is validated correctly when empty'() {
        when:
        uvt.validateReleaseCode(code)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'release.code\' has invalid value!'

        where:
        code << [null, '', '  \t', 'with letter', 'withletter', '123-123']
    }

    def 'release code is validated correctly when set'() {
        when:
        uvt.validateReleaseCode(code)

        then:
        noExceptionThrown()

        where:
        code << ['121', '1']
    }

    def 'release string is validated correctly when empty'() {
        when:
        uvt.validateReleaseString(code)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Property \'release.string\' has invalid value!'

        where:
        code << [null, '  ', '  \t', 'with\tletter', 'with space']
    }

    def 'release string is validated correctly when set'() {
        when:
        uvt.validateReleaseString(code)

        then:
        noExceptionThrown()

        where:
        code << ['releaseString', 'release_String', 'relase_String_123_4']
    }

    def 'version is updated correctly'() {
        given:
        def arc = GroovyMock(AndroidReleaseConfiguration)
        arc.releaseCode >> '3145'
        arc.releaseString >> '31.4.5'

        and:
        def amh = new AndroidManifestHelper()

        and:
        uvt.releaseConf = arc
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
