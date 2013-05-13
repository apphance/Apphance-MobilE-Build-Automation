package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.ios.IOSXCodeOutputParser

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
    @Inject
    IOSVariantsConfiguration iosVariantsConfiguration
    @Inject
    IOSReleaseConfiguration iosReleaseConfiguration

    @Override
    boolean isEnabled() {
        conf.enabled && enabledInternal
    }

    @Override
    void setEnabled(boolean enabled) {
        enabledInternal = enabled
    }

    def variantName = new StringProperty(
            name: 'ios.framework.variantName',
            message: 'Variant to build framework project with',
            possibleValues: { iosVariantsConfiguration.variantsNames.value }
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

    @Override
    boolean canBeEnabled() {
        !iosReleaseConfiguration.enabled
    }

    @Override
    String getMessage() {
        "'$configurationName' cannot be enabled because '${iosReleaseConfiguration.configurationName}' is enabled and those plugins are mutually exclusive.\n"
    }
}
