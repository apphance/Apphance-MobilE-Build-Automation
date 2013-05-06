package com.apphance.ameba.plugins.android

import spock.lang.Specification

class AndroidManifestHelperSpec extends Specification {

    def projectDir = new File('testProjects/android/android-basic')

    def 'project icon is found'() {
        given:
        def amh = new AndroidManifestHelper()

        expect:
        'icon' == amh.readIcon(projectDir)
    }
}
