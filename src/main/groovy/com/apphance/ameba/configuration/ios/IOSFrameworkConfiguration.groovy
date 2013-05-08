package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.IOSXCodeOutputParser
import org.gradle.api.GradleException

import javax.inject.Inject

@com.google.inject.Singleton
class IOSFrameworkConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Framework Configuration'
    private boolean enabledInternal = false

    @Inject
    IOSConfiguration conf
    @Inject
    IOSExecutor iosExecutor
    @Inject
    IOSXCodeOutputParser parser

    @Override
    boolean isEnabled() {
        conf.enabled && enabledInternal
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def target = new StringProperty(
            name: 'ios.framework.target',
            message: 'Target to build framework project with',
            possibleValues: { parser.readBaseTargets(trimmedListOutput(), { true }) as List<String> }
    )

    def configuration = new StringProperty(
            name: 'ios.framework.configuration',
            message: 'Configuration to build framework project with',
            defaultValue: { 'Debug' },
            possibleValues: { parser.readBaseConfigurations(trimmedListOutput(), { true }) as List<String> }
    )

    def version = new StringProperty(
            name: 'ios.framework.version',
            message: 'Version of framework (usually single alphabet letter A)',
            defaultValue: { 'A' }
    )

    def headers = new ListStringProperty(
            name: 'ios.framework.headers',
            message: 'List of headers (coma separated) that should be copied to the framework'
    )

    def resources = new ListStringProperty(
            name: 'ios.framework.resources',
            message: 'List of resources (coma separated) that should be copied to the framework'
    )

    List<String> trimmedListOutput() { //TODO cache output
        List<String> trimmedListOutput = iosExecutor.list()*.trim()
        if (trimmedListOutput.empty || trimmedListOutput[0] == '') {
            throw new GradleException("Error while running iosExecutor.list")
        }
        trimmedListOutput
    }
}
