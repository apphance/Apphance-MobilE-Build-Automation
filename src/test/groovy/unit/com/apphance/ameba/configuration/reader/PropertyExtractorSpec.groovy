package com.apphance.ameba.configuration.reader

import spock.lang.Specification

import static com.apphance.ameba.detection.ProjectType.IOS

class PropertyExtractorSpec extends Specification {

    def 'property extractor returns correct value'() {
        given:
        def pe = Mock(PropertyPersister)

        when:
        pe.get('project.type') >> IOS

        then:
        pe.get('project.type') == IOS

    }
}
