package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.plugins.ios.scheme.IOSSchemeInfo
import com.apphance.flow.util.FlowUtils
import com.google.inject.Singleton
import groovy.transform.PackageScope

import javax.inject.Inject

@Singleton
@Mixin(FlowUtils)
class IOSVariantsConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Variants Configuration'

    @Inject IOSConfiguration conf
    @Inject IOSVariantFactory variantFactory
    @Inject IOSSchemeInfo schemeInfo

    @Inject
    @Override
    void init() {
        super.init()
    }

    def variantsNames = new ListStringProperty(
            name: 'ios.variants',
            message: "Variants (first variant on the list will be considered as a 'main'",
            possibleValues: { possibleVariants },
            validator: {
                def list = variantsNames.convert(it.toString())
                list.size() == list.unique().size() && !list.isEmpty() && list.every { it in possibleVariants }
            }
    )

    @Lazy
    @PackageScope
    List<String> possibleVariants = {
        schemeInfo.schemeFiles.findAll { schemeInfo.schemeShared(it) }.collect { getNameWithoutExtension(it.name) }
    }()

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    @Override
    Collection<? extends AbstractIOSVariant> getSubConfigurations() {
        variantsInternal()
    }

    Collection<? extends AbstractIOSVariant> getVariants() {
        variantsInternal().findAll { it.isEnabled() }
    }

    AbstractIOSVariant getMainVariant() {
        variantsInternal()[0]
    }

    private List<? extends AbstractIOSVariant> variantsInternal() {
        variantsNames.value.collect {
            schemeVariant.call(it)
        }
    }

    @PackageScope
    Closure<? extends AbstractIOSVariant> schemeVariant = { String name ->
        variantFactory.createSchemeVariant(name)
    }.memoize()

    @Override
    boolean canBeEnabled() {
        schemeInfo.hasSchemes
    }

    @Override
    void checkProperties() {
        super.checkProperties()
    }
}
