package com.apphance.flow.plugins.android.parsers

import spock.lang.Specification

import static com.google.common.io.Files.createTempDir

class AndroidBuildXmlHelperSpec extends Specification {

    def helper = new AndroidBuildXmlHelper()
    def buildXML = new File(getClass().getResource('build.xml').toURI())
    def tmpDir = createTempDir()

    def setup() {
        new File(tmpDir, 'build.xml') << buildXML.text
    }

    def cleanup() {
        tmpDir.deleteDir()
    }

    def 'read name from build.xml'() {
        expect:
        'TestAndroidProject' == helper.projectName(tmpDir)
    }

    def 'replace project name in build.xml'() {
        when:
        helper.replaceProjectName(tmpDir, 'NewName')

        then:
        'NewName' == helper.projectName(tmpDir)
    }

    def 'empty project nam returned when no build.xml file'() {
        when:
        def tmpDir2 = createTempDir()

        then:
        '' == helper.projectName(tmpDir2)

        cleanup:
        tmpDir2.deleteDir()
    }
}
