package com.apphance.ameba.android

import spock.lang.Specification

import static com.google.common.io.Files.createTempDir

class AndroidBuildXmlHelperSpec extends Specification {

    def PROJECT_DIR = new File('testProjects/android/android-basic')
    def tmpDir = createTempDir()

    def setup() {
        new File(tmpDir, 'build.xml') << new File(PROJECT_DIR, 'build.xml').text
    }

    def cleanup() {
        tmpDir.delete()
    }

    def 'test read name from build.xml'() {
        expect:
        'TestAndroidProject' == AndroidBuildXmlHelper.projectName(PROJECT_DIR)
    }

    def 'test replace project name in build.xml'() {
        when:
        AndroidBuildXmlHelper.replaceProjectName(tmpDir, 'NewName')

        then:
        'NewName' == AndroidBuildXmlHelper.projectName(tmpDir)
    }
}
