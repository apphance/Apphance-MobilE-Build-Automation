package com.apphance.ameba.plugins.ios.parsers

import com.apphance.ameba.executor.IOSExecutor
import spock.lang.Specification

class PlistParserSpec extends Specification {

    def parser = new PlistParser()
    def executor

    def setup() {
        executor = GroovyMock(IOSExecutor)
        executor.plistToJSON(_) >> new File('testProjects/ios/GradleXCode/GradleXCode/GradleXCode-Info.plist.json').text.split('\n')

        parser.executor = executor
    }

    def 'version code is read correctly'() {
        expect:
        parser.versionCode(Mock(File)) == '32'
    }

    def 'version string is read correctly'() {
        expect:
        parser.versionString(Mock(File)) == '1.0'
    }

    def 'bundle id is read correctly'() {
        expect:
        parser.bundleId(Mock(File)) == 'com.apphance.ameba'
    }

    def 'bundleId is replaced correctly'() {
        given:
        def plist = new File('testProjects/ios/GradleXCode/GradleXCode/GradleXCode-Info.plist')

        when:
        def replaced = parser.replaceBundledId(plist, oldBundle, newBundle)

        then:
        replaced.contains(expected)
        def xml = new XmlSlurper().parseText(replaced)
        def keyNode = xml.dict.key.find { it.text() == 'CFBundleIdentifier' }
        def siblings = keyNode.parent().children()
        siblings[siblings.findIndexOf { it == keyNode } + 1].text() == expected

        where:
        newBundle                 | oldBundle            | expected
        'com.apphance.ameba2'     | 'com.apphance.ameba' | 'com.apphance.ameba2'
        'com.apphance.ameba.test' | 'com.apphance.ameba' | 'com.apphance.ameba.test'
        'com.app.ameba'           | 'com.apphance.ameba' | 'com.apphance.ameba'
        'pl.apphance.ameba'       | 'com.apphance.ameba' | 'com.apphance.ameba'
    }

    def 'versionCode and versionString are replaced correctly'() {
        given:
        def plist = new File('testProjects/ios/GradleXCode/GradleXCode/GradleXCode-Info.plist')

        when:
        def replaced = parser.replaceVersion(plist, '46', '2.0')

        then:
        def xml = new XmlSlurper().parseText(replaced)
        ['CFBundleVersion': '46', 'CFBundleShortVersionString': '2.0'].every { m ->
            def keyNode = xml.dict.key.find { it.text() == m.key }
            def siblings = keyNode.parent().children()
            siblings[siblings.findIndexOf { it == keyNode } + 1].text() == m.value
        }
    }

    def 'get icon files'() {
        when:
        def iconFiles = parser.getIconFiles(Mock(File))

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
}
