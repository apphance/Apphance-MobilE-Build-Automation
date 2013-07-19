package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class IOSSchemeInfoSpec extends Specification {

    @Shared
    def schemes = ['GradleXCode',
            'GradleXCode With Space',
            'GradleXCodeNoLaunchAction',
            'GradleXCodeWithApphance',
            'GradleXCodeWith2Targets',
            'GradleXCode 2']

    def xcodeDir = new File(getClass().getResource('iosProject').toURI())

    def conf = GroovyMock(IOSConfiguration) {
        getXcodeDir() >> new FileProperty(value: xcodeDir)
        getSchemes() >> schemes
    }
    def schemeInfo = new IOSSchemeInfo(conf: conf, schemeParser: new XCSchemeParser())

    def 'schemes are detected correctly'() {
        expect:
        schemeInfo.hasSchemes
        schemeInfo.schemesBuildable()
        schemeInfo.schemesDeclared()
        schemeInfo.schemesShared()
        schemeInfo.schemesHasEnabledTestTargets()
    }

    def 'schemes are detected correctly when not declared'() {
        given:
        schemeInfo.conf = GroovyMock(IOSConfiguration) {
            getSchemes() >> []
        }

        expect:
        !schemeInfo.hasSchemes
        !schemeInfo.schemesBuildable()
        !schemeInfo.schemesDeclared()
        !schemeInfo.schemesShared()
        !schemeInfo.schemesHasEnabledTestTargets()
    }


    @Unroll
    def 'scheme #scheme is buildable #buildable'() {
        expect:
        schemeInfo.schemeBuildable(schemeInfo.schemeFile.call(scheme)) == buildable

        where:
        scheme << schemes
        buildable << [true, true, false, true, true, false]
    }

    @Unroll
    def 'scheme #scheme is shared #shared'() {
        expect:
        schemeInfo.schemeShared(schemeInfo.schemeFile.call(scheme)) == shared

        where:
        scheme << schemes
        shared << [true, true, true, true, true, false]
    }

    @Unroll
    def 'scheme #scheme has enabled test targets #enabled'() {
        expect:
        schemeInfo.schemeHasEnabledTestTargets(schemeInfo.schemeFile.call(scheme)) == enabled

        where:
        scheme << schemes
        enabled << [true, false, false, false, false, false]
    }
}
