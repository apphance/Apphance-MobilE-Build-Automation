package com.apphance.flow.configuration.ios

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import org.gradle.api.Project
import spock.lang.Shared
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSConfigurationSpec extends Specification {

    @Shared
    def p = builder().withProjectDir(new File('testProjects/ios/GradleXCode')).build()
    @Shared
    def ic = new IOSConfiguration(project: p)

    def 'possible xcodeproj dirs found'() {
        expect:
        def pv = ic.xcodeDir.possibleValues()
        pv.size() > 0
        pv.contains('GradleXCode.xcodeproj')
    }

    def 'xcodeproj dir validator works well'() {
        given:
        def validator = ic.xcodeDir.validator

        expect:
        validator(val) == expectedResult

        where:
        val                     | expectedResult
        null                    | false
        ''                      | false
        '\n  '                  | false
        'non-existing'          | false
        'GradleXCode'           | false
        'GradleXCode.xcodeproj' | true
        'ico.png'               | false

    }

    def 'version code and string are taken from main variant'() {
        given:
        def variant = GroovyMock(IOSVariant) {
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
                    getMainVariant() >> GroovyMock(IOSVariant) { getProjectName() >> 'test project name' }
                }
        )

        expect:
        iOSConf.getProjectName() instanceof StringProperty
        iOSConf.getProjectName().value == 'test project name'
        iOSConf.getProjectName().toString() == 'test project name'
    }
}
