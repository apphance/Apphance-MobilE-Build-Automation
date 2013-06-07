package com.apphance.ameba.configuration.android

import spock.lang.Specification

class AndroidJarLibraryConfigurationSpec extends Specification {

    def 'mutual exclusion of release configuration and jar library configuration'() {
        given:
        def jarLibraryConf = new AndroidJarLibraryConfiguration()
        jarLibraryConf.releaseConf = GroovyStub(AndroidReleaseConfiguration)
        jarLibraryConf.releaseConf.enabled >> releaseEnabled

        expect:
        jarLibraryConf.canBeEnabled() ^ releaseEnabled

        where:
        releaseEnabled << [true, false]
    }
}
