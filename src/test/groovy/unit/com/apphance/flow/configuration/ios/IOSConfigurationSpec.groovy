package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import spock.lang.Specification

class IOSConfigurationSpec extends Specification {

    def 'version code and string are taken from main variant'() {
        given:
        def variant = GroovyMock(AbstractIOSVariant) {
            getVersionCode() >> 'version code'
            getVersionString() >> 'version string'
        }
        def conf = GroovyStub(IOSVariantsConfiguration) {
            getMainVariant() >> variant
        }
        def iOSConf = new IOSConfiguration()
        iOSConf.variantsConf = conf

        expect:
        with(iOSConf) {
            versionCode == 'version code'
            versionString == 'version string'
        }
    }

    def 'test get project name'() {
        given:
        def iOSConf = new IOSConfiguration(variantsConf:
                GroovyMock(IOSVariantsConfiguration) {
                    getMainVariant() >> GroovyMock(AbstractIOSVariant) { getProjectName() >> 'test project name' }
                }
        )

        expect:
        iOSConf.getProjectName() instanceof StringProperty
        iOSConf.getProjectName().value == 'test project name'
        iOSConf.getProjectName().toString() == 'test project name'
    }
}
