package com.apphance.ameba.plugins.ios.parsers

import com.apphance.ameba.executor.IOSExecutor
import spock.lang.Specification

class PbxJsonParserSpec extends Specification {

    def 'plist for configuration and blueprint is found correctly'() {
        given:
        def configuration = 'BasicConfiguration'
        def blueprintId = 'D382B71014703FE500E9CC9B'

        and:
        def input = new File('testProjects/ios/GradleXCode/GradleXCode.xcodeproj/project.pbxproj.json')

        and:
        def executor = Mock(IOSExecutor)
        executor.pbxProjToJSON() >> input.text.split('\n')

        and:
        def parser = new PbxJsonParser()
        parser.executor = executor

        expect:
        parser.plistForScheme(configuration, blueprintId) == 'GradleXCode/GradleXCode-Info.plist'
    }

    def 'plist for target and configuration is found correctly'() {
        given:
        def target = 'GradleXCode'
        def configuration = 'BasicConfiguration'

        and:
        def input = new File('testProjects/ios/GradleXCode/GradleXCode.xcodeproj/project.pbxproj.json')

        and:
        def executor = Mock(IOSExecutor)
        executor.pbxProjToJSON() >> input.text.split('\n')

        and:
        def parser = new PbxJsonParser()
        parser.executor = executor

        expect:
        parser.plistForTC(target, configuration) == 'GradleXCode/GradleXCode-Info.plist'
    }
}
