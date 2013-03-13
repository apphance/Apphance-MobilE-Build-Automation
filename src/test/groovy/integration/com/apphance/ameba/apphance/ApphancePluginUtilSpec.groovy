package com.apphance.ameba.apphance

import spock.lang.Shared
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class ApphancePluginUtilSpec extends Specification {

    @Shared
    def pluginUtil = new ApphancePluginUtil()

    def 'adds apphance configuration and repositories to project'() {
        given:
        def project = builder().build()

        expect:
        !project.configurations.asMap['apphance']
        !project.repositories

        when:
        pluginUtil.addApphanceConfiguration(project)

        then:
        project.configurations.asMap['apphance']
        [
                'https://dev.polidea.pl/artifactory/libs-releases-local/',
                'https://dev.polidea.pl/artifactory/libs-snapshots-local/'
        ].sort() ==
                project.repositories*.url*.toString().sort()
    }

    def 'prepares apphance lib dependency'() {
        given:
        def project = builder().build()

        expect:
        project.dependencies.properties['configurationContainer'].empty

        when:
        pluginUtil.addApphanceConfiguration(project)
        def dependency = pluginUtil.prepareApphanceLibDependency(project, 'com:corp:1.2')

        then:
        !project.dependencies.properties['configurationContainer'].empty
        project.configurations.apphance
        project.dependencies.properties['configurationContainer'].properties.asMap['apphance']
        'com:corp:1.2' == dependency
    }
}
