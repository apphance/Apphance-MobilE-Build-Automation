package com.apphance.flow.plugins.ios.xcodeproj

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.PbxJsonParser
import org.gradle.api.GradleException
import spock.lang.Shared
import spock.lang.Specification

class IOSXCodeprojLocatorSpec extends Specification {

    @Shared conf = GroovyMock(IOSConfiguration) {
        getRootDir() >> new File('demo/ios/GradleXCode')
    }
    @Shared pbxParser = new PbxJsonParser(executor: GroovyMock(IOSExecutor) {
        pbxProjToJSON(_) >> new File('demo/ios/GradleXCode/GradleXcode.xcodeproj/project.pbxproj.json').readLines()
    })
    @Shared xcodeprojInfo = new IOSXCodeprojLocator(conf: conf, pbxParser: pbxParser)

    def 'xcodeproj found for valid name and valid blueprintId'() {
        expect:
        xcodeprojInfo.findXCodeproj('GradleXCode.xcodeproj', 'D382B71014703FE500E9CC9B').path ==
                'demo/ios/GradleXCode/GradleXCode.xcodeproj'
    }

    def 'xcodeproj not found for valid name and invalid blueprintId'() {
        when:
        xcodeprojInfo.findXCodeproj('GradleXCode.xcodeproj', 'invalid')

        then:
        def e = thrown(GradleException)
        e.message == 'Impossible to find unique xcodeproj for name: GradleXCode.xcodeproj and blueprintId: invalid, found: []'
    }
}
