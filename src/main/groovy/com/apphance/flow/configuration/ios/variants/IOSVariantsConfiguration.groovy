package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.google.inject.Singleton
import groovy.transform.PackageScope

import javax.inject.Inject

import static IOSVariantType.SCHEME
import static IOSVariantType.TC
import static com.apphance.flow.configuration.properties.ListStringProperty.getSEPARATOR
import static org.apache.commons.lang.StringUtils.isNotBlank

@Singleton
class IOSVariantsConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Variants Configuration'

    private List<AbstractIOSVariant> variants

    @Inject IOSConfiguration conf
    @Inject IOSVariantFactory variantFactory
    @Inject XCSchemeParser schemeParser

    @Override
    @Inject
    void init() {
        super.init()
        variantType.value = variantType.value ?: (conf.schemes ? SCHEME : TC).name()
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
            //TODO validator
    )

    @PackageScope
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

    @PackageScope
    List<AbstractIOSVariant> extractVariantsFromProperties() {
        variantsNames.value.collect {
            variantType.value == SCHEME.name() ?
                variantFactory.createSchemeVariant(it)
            :
                variantFactory.createTCVariant(it)
        }
    }

    @PackageScope
    boolean hasSchemes() {
        conf.schemes.any { isNotBlank(it) }
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
        this.getVariants()
    }

    Collection<AbstractIOSVariant> getVariants() {
        this.@variants.findAll { it.name in variantsNames.value }
    }

    AbstractIOSVariant getMainVariant() {
        variants[0]
    }

    @Override
    void checkProperties() {
        super.checkProperties()
    }
}
