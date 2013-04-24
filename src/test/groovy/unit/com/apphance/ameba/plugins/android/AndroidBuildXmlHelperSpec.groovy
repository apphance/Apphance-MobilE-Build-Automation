package com.apphance.ameba.plugins.android

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
        tmpDir.delete()
    }

    def 'test read name from build.xml'() {
        expect:
        'TestAndroidProject' == helper.projectName(tmpDir)
    }

    def 'test replace project name in build.xml'() {
        when:
        helper.replaceProjectName(tmpDir, 'NewName')

        then:
        'NewName' == helper.projectName(tmpDir)
    }
}
