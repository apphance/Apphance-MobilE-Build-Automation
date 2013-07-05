package com.apphance.flow.plugins.ios.parsers

import org.gradle.api.GradleException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static com.google.common.io.Files.copy
import static com.google.common.io.Files.createTempDir

class XCSchemeParserSpec extends Specification {

    @Shared
    def schemeFile = new File(getClass().getResource('GradleXCode.xcscheme').toURI())

    def parser = new XCSchemeParser()

    def 'configuration for scheme is read correctly'() {
        expect:
        parser.configurationName(schemeFile) == 'BasicConfiguration'
    }

    def 'blueprintIdentifier for scheme is read correctly'() {
        expect:
        'D382B71014703FE500E9CC9B' == parser.blueprintIdentifier(schemeFile)
    }

    def 'buildableName for scheme is read correctly'() {
        expect:
        parser.buildableName(schemeFile) == 'GradleXCode.app'
    }

    def 'exception is thrown when no scheme file exists'() {
        given:
        def schemeFile = new File('random')

        when:
        parser.configurationName(schemeFile)

        then:
        def e = thrown(GradleException)
        e.message == "Shemes must be shared! Invalid scheme file: ${schemeFile.absolutePath}"
    }

    @Unroll
    def 'buildable scheme is recognized for scheme #scheme'() {
        expect:
        parser.isBuildable(scheme) == buildable

        where:
        scheme                                                            | buildable
        schemeFile                                                        | true
        new File(getClass().getResource('GradleXCode2.xcscheme').toURI()) | false
    }

    @Unroll
    def 'archive post action is added in file: #filename'() {
        given:
        def tmpDir = createTempDir()
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

        cleanup:
        tmpDir.deleteDir()

        where:
        filename       | actions
        'GradleXCode'  | 1
        'GradleXCode2' | 2
    }
}
