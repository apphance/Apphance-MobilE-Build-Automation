package com.apphance.flow.configuration.apphance

import spock.lang.Specification

class ApphanceLibTypeSpec extends Specification {

    def 'apphance lib type is returned correctly'() {
        expect:
        ApphanceLibType.libForMode(mode) == expectedMode

        where:
        mode                | expectedMode
        ApphanceMode.QA     | ApphanceLibType.PRE_PROD
        ApphanceMode.SILENT | ApphanceLibType.PRE_PROD
        ApphanceMode.PROD   | ApphanceLibType.PROD
    }
}
