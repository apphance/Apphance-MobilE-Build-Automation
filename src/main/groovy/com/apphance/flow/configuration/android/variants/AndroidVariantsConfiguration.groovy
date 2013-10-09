package com.apphance.flow.configuration.android.variants

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.android.AndroidBuildMode
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.google.inject.Singleton

import javax.inject.Inject

@Singleton
class AndroidVariantsConfiguration extends AbstractConfiguration {

    String configurationName = 'Android Variants Configuration'

    @Inject AndroidConfiguration conf
    @Inject AndroidVariantFactory variantFactory

    @Override
    @Inject
    void init() {
        super.init()
    }

    def variantsNames = new ListStringProperty(
            name: 'android.variants',
            message: "Variant names. Debug variant must contains word 'debug' (case insensitive) in name. Otherwise it is Release variant",
            defaultValue: { possibleVariants },
            possibleValues: { possibleVariants },
            validator: {
                def list = variantsNames.convert(it.toString())
                list.size() == list.unique().size() && !list.isEmpty()
            }
    )

    @Lazy
    private List<String> possibleVariants = {
        (variantsDirExistsAndIsNotEmpty ? variantsFromDir : defaultVariants)*.name
    }()

    @Lazy
    private List<AndroidVariantConfiguration> variantsFromDir = {
        [variantsDir.listFiles()*.name, AndroidBuildMode.values()*.capitalize()].combinations().collect { d, m ->
            variantFactory.create("$d$m", new File(variantsDir, d))
        }
    }()

    @Lazy
    private List<AndroidVariantConfiguration> defaultVariants = {
        AndroidBuildMode.values().collect { variantFactory.create(it.capitalize()) }
    }()

    @Lazy
    private boolean variantsDirExistsAndIsNotEmpty = {
        (variantsDir && variantsDir.isDirectory() && variantsDir.list().size() > 0)
    }()

    @Lazy
    File variantsDir = { new File(conf.rootDir, 'variants') }()

    @Lazy
    AndroidVariantConfiguration mainVariant = { variants[0] }()

    @Override
    Collection<AndroidVariantConfiguration> getSubConfigurations() {
        variantsInternal
    }

    List<AndroidVariantConfiguration> getVariants() {
        variantsInternal.findAll { it.enabled }
    }

    @Lazy
    private List<AndroidVariantConfiguration> variantsInternal = {
        variantsNames.value.collect { variantFactory.create(it) }
    }()

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    @Override
    void checkProperties() {
        defaultValidation variantsNames
    }
}
