package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.util.FlowUtils
import org.gradle.api.GradleException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.configuration.ios.variants.IOSXCodeAction.*
import static com.google.common.io.Files.copy

@Mixin(FlowUtils)
class XCSchemeParserSpec extends Specification {

    @Shared scheme1 = new File(getClass().getResource('GradleXCode.xcscheme').toURI())
    @Shared scheme2 = new File(getClass().getResource('GradleXCode2.xcscheme').toURI())
    @Shared parser = new XCSchemeParser()

    def 'configuration for scheme is read and action correctly'() {
        expect:
        parser.configuration(scheme1, LAUNCH_ACTION) == 'BasicConfiguration'

        where:
        action         | conf
        LAUNCH_ACTION  | 'BasicConfiguration'
        ARCHIVE_ACTION | 'Release'
        TEST_ACTION    | 'Debug'
    }

    def 'blueprintIdentifier for scheme is read correctly'() {
        expect:
        'D382B71014703FE500E9CC9B' == parser.blueprintIdentifier(scheme1)
    }

    def 'exception is thrown when no scheme file exists'() {
        given:
        def schemeFile = new File('random')

        when:
        parser.configuration(schemeFile, null)

        then:
        def e = thrown(GradleException)
        e.message == "Schemes must be shared! Invalid scheme file: ${schemeFile.absolutePath}"
    }

    @Unroll
    def 'buildable scheme is recognized for scheme #scheme'() {
        expect:
        parser.isBuildable(scheme) == buildable

        where:
        scheme  | buildable
        scheme1 | true
        scheme2 | false
    }

    @Unroll
    def 'archive post action is added in file: #filename'() {
        given:
        def tmpDir = temporaryDir
        copy(new File(getClass().getResource("${filename}.xcscheme").toURI()), new File(tmpDir, "${filename}.xcscheme"))
        def tmpFile = new File(tmpDir, "${filename}.xcscheme")

        when:
        parser.addPostArchiveAction(tmpFile)

        then:
        def xml = new XmlSlurper().parseText(tmpFile.text)
        xml.ArchiveAction.PostActions.children().size() == actions
        xml.ArchiveAction.PostActions.ExecutionAction.ActionContent.find {
            it.@title.text() == 'ECHO_FLOW_ARCHIVE_PATH'
        }
        xml.ArchiveAction.@revealArchiveInOrganizer == 'YES'

        where:
        filename       | actions
        'GradleXCode'  | 1
        'GradleXCode2' | 2
    }

    @Unroll
    def 'scheme (#scheme) is recognized as having single target'() {
        expect:
        parser.hasSingleBuildableTarget(scheme) == hasSingleBuildableTarget

        where:
        scheme  | hasSingleBuildableTarget
        scheme1 | true
        scheme2 | false
    }

    @Unroll
    def 'scheme (#scheme) has test targets as expected'() {
        expect:
        parser.hasEnabledTestTargets(scheme) == hasSingleBuildableTarget

        where:
        scheme  | hasSingleBuildableTarget
        scheme1 | true
        scheme2 | false
    }

    def 'testable targets found for given scheme file'() {
        expect:
        parser.findActiveTestableBlueprintIds(scheme) == targets

        where:
        scheme  | targets
        scheme1 | ['D382B73414703FE500E9CC9B']
        scheme2 | []
    }

    def 'xcodeproj name is returned'() {
        expect:
        parser.xcodeprojName(scheme1) == 'GradleXCode.xcodeproj'
    }
}
