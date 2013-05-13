package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.properties.ListStringProperty
import org.gradle.api.Project
import org.gradle.api.logging.Logging

import javax.inject.Inject

import static com.apphance.ameba.configuration.properties.ListStringProperty.getSEPARATOR

@com.google.inject.Singleton
class AndroidVariantsConfiguration extends AbstractConfiguration {

    def log = Logging.getLogger(this.class)

    String configurationName = 'Android variants configuration'

    @Inject
    Project project
    @Inject
    AndroidConfiguration conf
    @Inject
    ApphanceConfiguration apphanceConf
    @Inject
    AndroidVariantFactory variantFactory

    private List<AndroidVariantConfiguration> variants

    @Override
    @Inject
    void init() {
        super.init()
        this.variants = buildVariantsList()
    }

    def variantsNames = new ListStringProperty(
            name: 'android.variants',
            message: 'Variants',
            possibleValues: { variantsNames.value ?: [] }
    )

    private List<AndroidVariantConfiguration> buildVariantsList() {
        log.info "Building variant list"
        List<AndroidVariantConfiguration> result = []
        if (variantsNames.value) {
            result.addAll(extractVariantsFromProperties())
        } else if (variantsDirExistsAndIsNotEmpty()) {
            result.addAll(extractVariantsFromDir())
            variantsNames.value = result*.name.join(SEPARATOR)
        } else {
            result.addAll(extractDefaultVariants())
            variantsNames.value = result*.name.join(SEPARATOR)
        }
        log.info "Created following variants: $variantsNames"
        result
    }

    private List<AndroidVariantConfiguration> extractVariantsFromProperties() {
        variantsNames.value.collect { variantFactory.create(it) }
    }

    private boolean variantsDirExistsAndIsNotEmpty() {
        File variantsDir = getVariantsDir()
        (variantsDir && variantsDir.isDirectory() && variantsDir.list().size() > 0)
    }

    File getVariantsDir() {
        project.file('variants')
    }

    private List<AndroidVariantConfiguration> extractVariantsFromDir() {
        getVariantsDir().listFiles()*.name.collect { String dirName ->
            AndroidBuildMode.values().collect { it ->
                variantFactory.create(dirName.toLowerCase().capitalize() + it.capitalize())
            }
        }.flatten()
    }

    private List<AndroidVariantConfiguration> extractDefaultVariants() {
        AndroidBuildMode.values().collect { variantFactory.create(it.capitalize()) }
    }


    @Override
    Collection<AndroidVariantConfiguration> getSubConfigurations() {
        variants
    }

    String getMainVariant() {
        variantsNames.value?.empty ? null : variantsNames.value[0]
    }

    Collection<AndroidVariantConfiguration> getVariants() {
        this.@variants
    }

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    @Override
    void checkProperties() {
        check variantsNames.value.sort() == variants*.name.sort(), "List in '${variantsNames.name}' property is not equal to the list of names of configured variants, check 'ameba.properties' file!"
    }
}
