package com.apphance.ameba.configuration.ios

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.properties.ListStringProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyPersister

import javax.inject.Inject

import static com.apphance.ameba.configuration.ios.IOSVariantsConfiguration.IOSVariantType.SCHEME
import static com.apphance.ameba.configuration.ios.IOSVariantsConfiguration.IOSVariantType.TC

@com.google.inject.Singleton
class IOSVariantsConfiguration extends AbstractConfiguration {

    String configurationName = 'IOS variants configuration'
    private List<AbstractIOSVariant> variants

    @Inject
    IOSConfiguration conf
    @Inject
    ApphanceConfiguration apphanceConf
    @Inject
    PropertyPersister persister

    enum IOSVariantType {
        SCHEME, TC

        static List<String> names() {
            values()*.name()
        }
    }

    @Override
    @Inject
    def init() {
        super.init()
        this.variants = buildVariantsList()
        variantType.value = (conf.schemes ? SCHEME : TC).name()
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

    List<AbstractIOSVariant> buildVariantsList() {
        readFromConfiguration() ?: extractDefaultVariants()
    }

    @groovy.transform.PackageScope
    List<AbstractIOSVariant> readFromConfiguration() {
        variantsNames.value.collect {
            variantType.value == SCHEME.name() ?
                new IOSSchemeVariant(it, conf, apphanceConf, persister) : new IOSTCVariant(it, conf, apphanceConf, persister)
        }
    }

    @groovy.transform.PackageScope
    List<AbstractIOSVariant> extractDefaultVariants() {
        def schemes = conf.schemes
        if (schemes) {
            schemes.collect { new IOSSchemeVariant(it, conf, apphanceConf, persister) }
        } else {
            [conf.targets, conf.configurations].combinations().sort().collect {
                t, c -> new IOSTCVariant("$t$c", this.conf, apphanceConf, persister)
            }
        }
    }

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    @Override
    Collection<AndroidVariantConfiguration> getSubConfigurations() {
        this.@variants
    }

    @Override
    void checkProperties() {
    }
}
