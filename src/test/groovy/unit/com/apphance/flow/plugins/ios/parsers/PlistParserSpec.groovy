package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.executor.IOSExecutor
import spock.lang.Specification

import static com.google.common.io.Files.copy
import static com.google.common.io.Files.createTempDir

class PlistParserSpec extends Specification {

    def parser = new PlistParser()

    def setup() {
        parser.executor = GroovyMock(IOSExecutor) {
            plistToJSON(_) >> new File('testProjects/ios/GradleXCode/GradleXCode/GradleXCode-Info.plist.json').text.split('\n')
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
        parser.bundleId(Mock(File)) == 'com.apphance.ameba'
    }

    def 'versionCode and versionString are replaced correctly'() {
        given:
        def plist = new File('testProjects/ios/GradleXCode/GradleXCode/GradleXCode-Info.plist')

        and:
        def tmpDir = createTempDir()
        def tmpPlist = new File(tmpDir, 'tmp.plist')

        and:
        copy(plist, tmpPlist)

        when:
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

    def 'placeholder is recognized correctly'() {
        expect:
        PlistParser.isPlaceholder(placeholder) == expected

        where:
        placeholder | expected
        '${}'       | false
        ''          | false
        '  \t'      | false
        '$${}'      | false
        '${{}'      | false
        '${}}'      | false
        '${_}'      | false
        '${AA_}'    | false
        '${AA_D}'   | true
        '${AA_D_}'  | false
        '${_AA_D_}' | false
    }

    def 'not a placeholder is recognized correctly'() {
        expect:
        PlistParser.isNotPlaceHolder(placeholder) == expected

        where:
        placeholder | expected
        '${}'       | true
        ''          | true
        '  \t'      | true
        '$${}'      | true
        '${{}'      | true
        '${}}'      | true
        '${_}'      | true
        '${AA_}'    | true
        '${AA_D}'   | false
        '${AA_D_}'  | true
        '${_AA_D_}' | true
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
