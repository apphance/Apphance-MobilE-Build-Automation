package com.apphance.ameba

import spock.lang.Specification

class XmlBomAwareFileReaderSpec extends Specification {

    def 'reads xml file with bom'() {
        given:
        def fileUrl = getClass().getResource('testBom.plist')
        and:
        def file = new File(fileUrl.file)

        when:
        def element = new XMLBomAwareFileReader().readXMLFileIncludingBom(file)

        then:
        element
    }
}
