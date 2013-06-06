package com.apphance.ameba.plugins

import org.gradle.api.GradleException
import org.gradle.api.Project
import spock.lang.Specification

class AmebaPluginSpec extends Specification {

    def 'exception is thrown when JRE version is too low'() {
        given:
        def proj = GroovySpy(Project)

        and:
        System.properties['java.version'] = '1.6.23_10'

        and:
        def plugin = new AmebaPlugin()

        when:
        plugin.apply(proj)

        then:
        def e = thrown(GradleException)
        e.message == 'Invalid JRE version: 1.6.23! Minimal JRE version is: 1.7'
    }
}
