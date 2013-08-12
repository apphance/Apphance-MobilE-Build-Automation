package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.executor.IOSExecutor
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.plugins.ios.apphance.IOSApphanceEnhancer.getAPPHANCE_FRAMEWORK_NAME_PATTERN

class PbxJsonParserSpec extends Specification {

    @Shared
    def input = new File('testProjects/ios/GradleXCode/GradleXCode.xcodeproj/project.pbxproj.json')
    @Shared
    def parser = new PbxJsonParser(executor: GroovyMock(IOSExecutor) {
        pbxProjToJSON(_) >> input.text.split('\n')
    })

    def 'plist for configuration and blueprint is found correctly'() {
        given:
        def configuration = 'BasicConfiguration'
        def blueprintId = 'D382B71014703FE500E9CC9B'

        expect:
        parser.plistForScheme(GroovyMock(File), configuration, blueprintId) == 'GradleXCode/GradleXCode-Info.plist'
    }

    def 'plist for target and configuration is found correctly'() {
        given:
        def target = 'GradleXCode'
        def configuration = 'BasicConfiguration'

        expect:
        parser.plistForTC(GroovyMock(File), target, configuration) == 'GradleXCode/GradleXCode-Info.plist'
    }

    def 'target name is found for blueprint id'() {
        expect:
        parser.targetForBlueprintId(GroovyMock(File), blueprintId) == target

        where:
        target             | blueprintId
        'GradleXCode'      | 'D382B71014703FE500E9CC9B'
        'GradleXCodeTests' | 'D382B73414703FE500E9CC9B'
    }

    def 'placeholder is recognized correctly'() {
        expect:
        PbxJsonParser.isPlaceholder(placeholder) == expected

        where:
        placeholder | expected
        '$()'       | false
        ''          | false
        '  \t'      | false
        '$$()'      | false
        '$(()'      | false
        '$())'      | false
        '$(_)'      | false
        '$(AA_)'    | false
        '$(AA_D)'   | true
        '$(AA_D_)'  | false
        '$(_AA_D_)' | false
    }

    def 'apphance framework is found correctly'() {
        given:
        parser.executor = GroovyMock(IOSExecutor) {
            pbxProjToJSON(_) >> parsedJSON
        }

        expect:
        parser.isFrameworkDeclared(GroovyMock(File), APPHANCE_FRAMEWORK_NAME_PATTERN) == frameworkDeclared

        where:
        frameworkDeclared | parsedJSON
        true              | new File(getClass().getResource('project.pbxproj.with.apphance.json').toURI()).text.split('\n')
        false             | input.text.split('\n')
    }
}
