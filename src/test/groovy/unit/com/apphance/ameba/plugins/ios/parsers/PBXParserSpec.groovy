package com.apphance.ameba.plugins.ios.parsers

import spock.lang.Specification

class PBXParserSpec extends Specification {

    def parser = new PBXParser()

    def 'plist for configuration is found correctly'() {
        given:
        def confName = 'BasicConfiguration'

        expect:
        parser.plist(confName) == 'bolo'
    }
}
