package com.apphance.ameba.configuration.ios.variants

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty

import javax.inject.Inject

import static IOSVariantType.SCHEME
import static IOSVariantType.TC
import static com.apphance.ameba.configuration.properties.ListStringProperty.getSEPARATOR

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
        super.init()
        variantType.value ?: (conf.schemes ? SCHEME : TC).name()
        this.variants = buildVariantsList()
    }

    def variantType = new StringProperty(
            name: 'ios.variants.type',
            interactive: { false },
            validator: { it in IOSVariantType.names() }
    )

    def variantsNames = new ListStringProperty(
            name: 'ios.variants',
            message: "Variants (first variant on the list will be considered as a 'main'",
            possibleValues: { variantsNames.value ?: [] }
    )

    @groovy.transform.PackageScope
    List<AbstractIOSVariant> buildVariantsList() {
        List<AbstractIOSVariant> result = []
        if (variantsNames.value) {
            result.addAll(extractVariantsFromProperties())
        } else if (hasSchemes()) {
            result.addAll(createVariantsFromSchemes())
            variantsNames.value = result*.name.join(SEPARATOR)
        } else {
            result.addAll(createVariantsFromTargetsAndConfigurations())
            variantsNames.value = result*.name.join(SEPARATOR)
        }
        result
    }

    @groovy.transform.PackageScope
    List<AbstractIOSVariant> extractVariantsFromProperties() {
        variantsNames.value.collect {
            variantType.value == SCHEME.name() ?
                variantFactory.createSchemeVariant(it)
            :
                variantFactory.createTCVariant(it)
        }
    }

    private boolean hasSchemes() {
        !(conf.schemes.findAll { it != null && !it?.trim()?.empty }).empty
    }

    private List<AbstractIOSVariant> createVariantsFromSchemes() {
        conf.schemes.collect { variantFactory.createSchemeVariant(it) }
    }

    private List<AbstractIOSVariant> createVariantsFromTargetsAndConfigurations() {
        conf.targetConfigurationMatrix.collect { t, c -> "$t$c" }.collect { variantFactory.createTCVariant(it.toString()) }
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

    AbstractIOSVariant getMainVariant() {
        variants[0]
    }

    @Override
    void checkProperties() {
    }
}
