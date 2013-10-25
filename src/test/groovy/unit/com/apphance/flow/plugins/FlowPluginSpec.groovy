package com.apphance.flow.plugins

import com.apphance.flow.util.FlowUtils
import com.apphance.flow.validation.ConfigurationValidator
import org.gradle.api.GradleException
import org.gradle.api.Project
import spock.lang.Specification

@Mixin(FlowUtils)
class FlowPluginSpec extends Specification {

    def 'exception is thrown when JRE version is too low'() {
        given:
        def proj = GroovySpy(Project)

        and:
        System.properties['java.version'] = '1.6.23_10'

        and:
        def plugin = GroovySpy(FlowPlugin) {
            flowVersion(_) >> '1.0'
        }

        when:
        plugin.apply(proj)

        then:
        def e = thrown(GradleException)
        e.message == 'Invalid JRE version: 1.6.23! Minimal JRE version is: 1.7'
    }

    def 'version from filename'() {
        given:
        def plugin = new FlowPlugin()

        expect:
        version == plugin.extractVersionFromFilename(fileName)

        where:
        fileName                         | version
        'apphance-flow-1.0.3.jar'        | '1.0.3'
        'apphance-flow-1.0.jar'          | '1.0'
        'apphance-flow-1.0-RC1.jar'      | '1.0-RC1'
        'apphance-flow-1.0-SNAPSHOT.jar' | '1.0-SNAPSHOT'
        'apphance-flow-1.0.3.4.5-M4.jar' | '1.0.3.4.5-M4'
        'flow-1.0.3.4.5-M4.jar'          | '1.0.3.4.5-M4'
        'flow-.jar'                      | ''
        ''                               | ''
        null                             | ''
    }

    def 'configuration validation is run depending on flow.properties existence'() {
        given:
        def plugin = new FlowPlugin()

        and:
        plugin.configurations = [:]

        and:
        plugin.configurationValidator = GroovyMock(ConfigurationValidator)

        when:
        plugin.validateConfiguration(props)

        then:
        count * plugin.configurationValidator.validate(plugin.configurations.values())

        where:
        props              | count
        null               | 0
        tempFile           | 0
        tempFile << 'text' | 1
    }
}
