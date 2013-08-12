package com.apphance.flow.configuration.android.variants

import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.configuration.apphance.ApphanceMode.PROD
import static com.apphance.flow.configuration.apphance.ApphanceMode.QA

class AndroidVariantConfigurationSpec extends Specification {

    @Unroll
    def 'test get apphance versions from maven. mode: #mode'() {
        given:
        def conf = new AndroidVariantConfiguration('name')

        when:
        List<String> versions = conf.parseVersionsFromMavenMetadata(mode)

        then:
        versions
        versions.containsAll(expectedVersions)

        where:
        mode | expectedVersions
        PROD | ['1.9']
        QA   | ['1.9', '1.9.1', '1.9.2']
    }
}
