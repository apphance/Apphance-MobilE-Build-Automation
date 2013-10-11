package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.executor.command.CommandExecutor
import com.apphance.flow.executor.command.CommandLogFilesGenerator
import com.apphance.flow.executor.linker.FileLinker
import spock.lang.Specification

import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static com.google.common.io.Files.copy
import static com.google.common.io.Files.createTempDir
import static java.io.File.createTempFile

class PlistParserSpec extends Specification {

    def parser = new PlistParser()

    def setup() {
        parser.executor = GroovyMock(IOSExecutor) {
            plistToJSON(_) >> new File(getClass().getResource('Test.plist.json').toURI()).text.split('\n')
            buildSettings(_, _) >> [
                    VALUE: 'value',
                    VALUE2: 'value_2'
            ]
        }

    }

    def 'version code is read correctly'() {
        expect:
        parser.bundleVersion(Mock(File)) == '32'
    }

    def 'version string is read correctly'() {
        expect:
        parser.bundleShortVersionString(Mock(File)) == '1.0'
    }

    def 'bundle id is read correctly'() {
        expect:
        parser.bundleId(Mock(File)) == 'com.apphance.flow'
    }

    def 'versionCode and versionString are replaced correctly'() {
        given:
        def tmpDir = createTempDir()
        def tmpPlist = new File(tmpDir, 'tmp.plist')
        def plist = new File('demo/ios/GradleXCode/GradleXCode/GradleXCode-Info.plist')
        def logFiles = [(STD): createTempFile('tmp', 'file-out'), (ERR): createTempFile('tmp', 'file-err')]
        def executor = new CommandExecutor(Mock(FileLinker) {
            fileLink(_) >> ['']
        }, Mock(CommandLogFilesGenerator) {
            commandLogFiles() >> logFiles
        })
        def iosExecutor = new IOSExecutor(executor: executor, conf: GroovyStub(IOSConfiguration) {
            getRootDir() >> tmpPlist.parentFile
        })

        and:
        copy(plist, tmpPlist)

        when:
        parser.executor = iosExecutor
        parser.replaceVersion(tmpPlist, '46', '2.0')

        then:
        def xml = new XmlSlurper().parse(tmpPlist)
        ['CFBundleVersion': '46', 'CFBundleShortVersionString': '2.0'].every { m ->
            def keyNode = xml.dict.key.find { it.text() == m.key }
            def siblings = keyNode.parent().children()
            siblings[siblings.findIndexOf { it == keyNode } + 1].text() == m.value
        }

        cleanup:
        tmpDir.deleteDir()
        logFiles.each {
            it.value.delete()
        }
    }

    def 'get icon files'() {
        when:
        def iconFiles = parser.iconFiles(Mock(File))

        then:
        iconFiles == ['icon.png', 'icon_retina.png']
    }

    def 'bundle display name is read correctly'() {
        expect:
        parser.bundleDisplayName(Mock(File)) == '${PRODUCT_NAME}'
    }

    def 'placeholders are evaluated correctly'() {
        expect:
        expected == parser.evaluate(value, 'target', 'conf')

        where:
        expected            | value
        'value'             | '${VALUE}'
        'value.1'           | '${VALUE}.1'
        'value.1.value_2.2' | '${VALUE}.1.${VALUE2}.2'
        'value.1.value2.2'  | '${VALUE}.1.${VALUE2:rfc1034identifier}.2'
        'value2.1'          | '${VALUE2:rfc1034identifier}.1'
        'value'             | 'value'
        '1_42'              | '1_42'
        null                | null
    }

    def 'rfc1034 identifier works well'() {
        expect:
        PlistParser.IDENTIFIERS['rfc1034identifier'](value) == expected

        where:
        value                | expected
        'value'              | 'value'
        'value2 value_3'     | 'value2value3'
        'value2-value_3'     | 'value2-value3'
        'value2-value_3.com' | 'value2-value3.com'
    }
}
