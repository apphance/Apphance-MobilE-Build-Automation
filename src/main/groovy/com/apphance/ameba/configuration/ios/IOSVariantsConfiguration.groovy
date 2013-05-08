package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.executor.IOSExecutor
import com.google.inject.Inject

import static com.apphance.ameba.plugins.ios.IOSXCodeOutputParser.*

@com.google.inject.Singleton
class IOSVariantsConfiguration extends AbstractConfiguration {

    String configurationName = 'IOS variants configuration'
    List<AbstractIOSVariant> variants

    @Inject
    IOSExecutor iosExecutor

    @Inject
    IOSConfiguration iosConf

    enum IOSVariantType { SCHEME, TC}

    @Override
    def init() {
        super.init()
        this.variants = buildVariantsList()
    }

    def variantsNames = new ListStringProperty(
            name: 'ios.variants',
            message: 'Variants',
            possibleValues: { variantsNames.value ?: [] }
    )

    def variantType = new StringProperty(
            name: 'ios.variants.type',
            message: 'Variant type. [Scheme|TC]',
            possibleValues: { IOSVariantType.values()*.toString() }
    )

    List<AbstractIOSVariant> buildVariantsList() {
        readFromConfiguration() ?: extractDefaultVariants()
    }

    @groovy.transform.PackageScope
    List<AbstractIOSVariant> readFromConfiguration() {
        variantsNames.value.collect {variantType.value == 'Scheme' ? new IOSSchemeVariant(it) : new IOSTCVariant(it)}
    }

    @groovy.transform.PackageScope
    List<AbstractIOSVariant> extractDefaultVariants() {
        def list = iosExecutor.list()
        def schemes = readSchemes(list)
        if (schemes) {
            schemes.collect { new IOSSchemeVariant(it) }
        } else {
            def targets = readBaseTargets(list) // TODO replace with iosConf.allTargets
            def configurations = readBaseConfigurations(list) // TODO replace with iosConf.allConfigurations

            [targets, configurations].combinations().sort().collect { target, conf -> new IOSTCVariant(target, conf) }
        }
    }

    @Override
    boolean isEnabled() {
        iosConf.enabled
    }

    @Override
    Collection<AndroidVariantConfiguration> getSubConfigurations() {
        variants
    }

    @Override
    void checkProperties() {
    }
}
