package com.apphance.flow.configuration.apphance

import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.configuration.apphance.ApphanceMode.*

class ApphanceArtifactorySpec extends Specification {

    @Shared
    def apphanceArtifactory = new ApphanceArtifactory()

    def 'android lib versions are filled'() {
        when:
        def libs = apphanceArtifactory.androidLibraries(QA)

        then:
        libs.containsAll('1.8.2', '1.9-RC1')

        when:
        libs = apphanceArtifactory.androidLibraries(SILENT)

        then:
        libs.containsAll('1.8.2', '1.9-RC1')

        when:
        libs = apphanceArtifactory.androidLibraries(PROD)

        then:
        libs.contains('1.8.2')
    }

    def 'exception thrown when bad mode passed for android libs'() {
        when:
        apphanceArtifactory.androidLibraries(DISABLED)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Invalid apphance mode: DISABLED'
    }

    def 'iOS lib versions are filled'() {
        when:
        def libs = apphanceArtifactory.iOSLibraries(QA, 'armv6')

        then:
        libs.contains('1.7')

        when:
        libs = apphanceArtifactory.iOSLibraries(SILENT, 'armv6')

        then:
        libs.contains('1.7')

        when:
        libs = apphanceArtifactory.iOSLibraries(QA, 'armv7')

        then:
        libs.containsAll('1.8.2', '1.8.8')

        when:
        libs = apphanceArtifactory.iOSLibraries(SILENT, 'armv7')

        then:
        libs.containsAll('1.8.2', '1.8.8')

        when:
        libs = apphanceArtifactory.iOSLibraries(PROD, 'armv6')

        then:
        libs.contains('1.7')

        when:
        libs = apphanceArtifactory.iOSLibraries(PROD, 'armv7')

        then:
        libs.containsAll('1.8.2', '1.8.8')
    }

    def 'exception thrown when bad mode passed for iOS libs'() {
        when:
        apphanceArtifactory.iOSLibraries(DISABLED, null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Invalid apphance mode: DISABLED'
    }

    def 'exception thrown when empty arch passed for iOS libs'() {
        when:
        apphanceArtifactory.iOSLibraries(QA, null)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == 'Invalid arch: null'
    }

    def 'exception thrown when bad arch passed for iOS libs'() {
        when:
        def libs = apphanceArtifactory.iOSLibraries(QA, 'armv3145')

        then:
        noExceptionThrown()
        libs == []
    }

    def 'iOS architectures downloaded correctly for mode'() {
        expect:
        archs == apphanceArtifactory.iOSArchs(mode)

        where:
        mode   | archs
        QA     | ['armv6', 'armv7']
        SILENT | ['armv6', 'armv7']
        PROD   | ['armv6', 'armv7']
    }
}
