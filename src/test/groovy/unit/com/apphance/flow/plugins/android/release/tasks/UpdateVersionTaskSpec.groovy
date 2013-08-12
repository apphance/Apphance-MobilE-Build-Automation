package com.apphance.flow.plugins.android.release.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import org.gradle.api.GradleException
import spock.lang.Specification

import static AndroidManifestHelper.ANDROID_MANIFEST
import static com.google.common.io.Files.copy
import static org.gradle.testfixtures.ProjectBuilder.builder

@Mixin(TestUtils)
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
        def tmpDir = temporaryDir
        def ac = GroovySpy(AndroidConfiguration) {
            getRootDir() >> tmpDir
        }
        ac.reader = GroovyStub(PropertyReader) {
            systemProperty('version.code') >> '3145'
            systemProperty('version.string') >> '31.4.5'
        }

        and:
        def amh = new AndroidManifestHelper()

        and:
        uvt.conf = ac
        uvt.manifestHelper = amh

        and:
        copy(new File(projectDir, ANDROID_MANIFEST), new File(tmpDir, ANDROID_MANIFEST))

        when:
        uvt.updateVersion()

        then:
        def version = amh.readVersion(tmpDir)
        '3145' == version.versionCode
        '31.4.5' == version.versionString
    }
}
