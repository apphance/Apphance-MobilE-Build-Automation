package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
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
                list.size() == list.unique().size() && !list.isEmpty()
            }
    )

    @Lazy
    @PackageScope
    List<String> possibleVariants = {
        if (schemeInfo.hasSchemes)
            return schemeInfo.schemeFiles.findAll {
                schemeInfo.schemeShared(it) &&
                        schemeInfo.schemeBuildable(it) &&
                        schemeInfo.schemeHasSingleBuildableTarget(it)
            }.collect { getNameWithoutExtension(it.name) }
        []
    }()

    private List<IOSVariant> variantsInternal() {
        variantsNames.value.collect {
            schemeVariant.call(it)
        }
    }

    @PackageScope
    Closure<IOSVariant> schemeVariant = { String name ->
        variantFactory.createSchemeVariant(name)
    }.memoize()

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    @Override
    Collection<IOSVariant> getSubConfigurations() {
        variantsInternal()
    }

    Collection<IOSVariant> getVariants() {
        variantsInternal()
    }

    IOSVariant getMainVariant() {
        variantsInternal()[0]
    }

    @Override
    boolean canBeEnabled() {
        schemeInfo.hasSchemes
    }

//        """To enable '${configurationName}' project:
//           - must have shared schemes
//           - schemes must be buildable (Executable set in scheme's 'Run Action')
//           - schemes must have single buildable target
//        """


    @Override
    void checkProperties() {
        super.checkProperties()
    }
}
