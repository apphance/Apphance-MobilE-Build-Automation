package com.apphance.ameba.plugins.release

import spock.lang.Specification

class ProjectReleaseCategorySpec extends Specification {

    def 'splits url'() {

        given:
        def baseUrl
        def directory

        when:
        (baseUrl, directory) = ProjectReleaseCategory.splitUrl(url)

        then:
        excpectedBaseUrl == baseUrl
        expectedDirectory == directory


        where:
        url                               | excpectedBaseUrl                       | expectedDirectory
        "http://www.example.com/ext/test" | new URL("http://www.example.com/ext/") | 'test'
        "http://www.example.com/test"     | new URL("http://www.example.com/")     | 'test'
        "http://www.example.com/test/"    | new URL("http://www.example.com/")     | 'test'
    }
}
