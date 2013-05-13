package com.apphance.ameba.plugins.ios.parsers

import com.apphance.ameba.executor.IOSExecutor
import spock.lang.Specification

class PlistParserSpec extends Specification {

    def parser = new PlistParser()
    def executor

    def setup() {
        executor = Mock(IOSExecutor)
        executor.plistToJSON(_) >> new File('testProjects/ios/GradleXCode/GradleXCode/GradleXCode-Info.plist.json').text.split('\n')

        parser.executor = executor
    }

    def 'version code is read correctly'() {
        expect:
        parser.getVersionCode(Mock(File)) == '32'
    }

    def 'version string is read correctly'() {
        expect:
        parser.getVersionString(Mock(File)) == '1.0'
    }
}
