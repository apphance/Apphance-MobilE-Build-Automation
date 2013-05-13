package com.apphance.ameba.configuration.ios.variants

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty

import javax.inject.Inject

import static IOSVariantType.SCHEME
import static IOSVariantType.TC

@com.google.inject.Singleton
class IOSVariantsConfiguration extends AbstractConfiguration {

    String configurationName = 'IOS variants configuration'

    private List<AbstractIOSVariant> variants

    @Inject
    IOSConfiguration conf

    @Inject
    IOSVariantFactory variantFactory

    @Override
    @Inject
    void init() {

        this.variants = buildVariantsList()
        variantType.value = (conf.schemes ? SCHEME : TC).name()

        super.init()
    }

    def variantType = new StringProperty(
            name: 'ios.variants.type',
            interactive: { false },
            validator: { it in IOSVariantType.names() }
    )

    def variantsNames = new ListStringProperty(
            name: 'ios.variants',
            message: 'Variants',
            possibleValues: { variantsNames.value ?: [] }
    )

    @groovy.transform.PackageScope
    List<AbstractIOSVariant> buildVariantsList() {
        readFromConfiguration() ?: extractDefaultVariants()
    }

    @groovy.transform.PackageScope
    List<AbstractIOSVariant> readFromConfiguration() {
        variantsNames.value.collect {
            variantType.value == SCHEME.name() ?
                variantFactory.createSchemeVariant(it)
            :
                variantFactory.createTCVariant(it)
        }
    }

    @groovy.transform.PackageScope
    List<AbstractIOSVariant> extractDefaultVariants() {
        def schemes = conf.schemes
        if (schemes) {
            variantsNames.value = schemes
            variantsNames.value.collect { variantFactory.createSchemeVariant(it) }
        } else {
            variantsNames.value = conf.targetConfigurationMatrix.collect { t, c -> "$t$c" }
            variantsNames.value.collect {
                variantFactory.createTCVariant(it)
            }
        }
    }

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    @Override
    Collection<AbstractIOSVariant> getSubConfigurations() {
        this.@variants
    }

    Collection<AbstractIOSVariant> getVariants() {
        this.@variants
    }

    String getMainVariant() {
        variantsNames.value?.empty ? null : variantsNames.value[0]
    }

    @Override
    void checkProperties() {
    }
}
