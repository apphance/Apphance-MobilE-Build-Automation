package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.google.inject.Singleton
import groovy.transform.PackageScope

import javax.inject.Inject

import static org.apache.commons.lang.StringUtils.isNotBlank

@Singleton
class IOSVariantsConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Variants Configuration'

    @Inject IOSConfiguration conf
    @Inject IOSVariantFactory variantFactory
    @Inject XCSchemeParser schemeParser

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
        if (hasSchemes) {
            conf.schemes.findAll { isNotBlank(it) && schemeParser.isBuildable(it) }
        } else {
            conf.targetConfigurationMatrix.collect { t, c -> "$t$c".toString() }
        }
    }()

    @Lazy
    @PackageScope
    boolean hasSchemes = {
        conf.schemes.any { isNotBlank(it) && schemeParser.isBuildable(it) }
    }()

    private List<AbstractIOSVariant> variantsInternal() {
        variantsNames.value.collect {
            if (hasSchemes)
                schemeVariant.call(it)
            else
                tcVariant.call(it)
        }
    }

    @PackageScope
    Closure<IOSSchemeVariant> schemeVariant = { String name ->
        variantFactory.createSchemeVariant(name)
    }.memoize()

    @PackageScope
    Closure<IOSTCVariant> tcVariant = { String name ->
        variantFactory.createTCVariant(name)
    }.memoize()

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    @Override
    Collection<AbstractIOSVariant> getSubConfigurations() {
        variantsInternal()
    }

    Collection<AbstractIOSVariant> getVariants() {
        variantsInternal()
    }

    AbstractIOSVariant getMainVariant() {
        variantsInternal()[0]
    }

    @Override
    void checkProperties() {
        super.checkProperties()
    }
}
