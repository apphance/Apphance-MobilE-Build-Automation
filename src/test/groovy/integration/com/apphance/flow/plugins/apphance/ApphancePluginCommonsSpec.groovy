package com.apphance.flow.plugins.apphance

import spock.lang.Shared
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class ApphancePluginCommonsSpec extends Specification {

    @Shared
    def pluginUtil = new ApphancePluginCommons()

    def 'adds apphance configuration and repositories to project'() {
        given:
        def project = builder().build()

        expect:
        !project.configurations.asMap['apphance']
        !project.repositories

        when:
        pluginUtil.addApphanceConfiguration(project, 'apphance')

        then:
        project.configurations.asMap['apphance']
        [
                'https://dev.polidea.pl/artifactory/libs-releases-local/',
                'https://dev.polidea.pl/artifactory/libs-snapshots-local/'
        ].sort() ==
                project.repositories*.url*.toString().sort()
    }
}
