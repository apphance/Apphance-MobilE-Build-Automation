package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.ListStringProperty
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.configuration.properties.ListStringProperty.getSEPARATOR

@com.google.inject.Singleton
class AndroidVariantsConfiguration extends AbstractConfiguration {

    String configurationName = 'Android variants configuration'

    private Project project
    private AndroidConfiguration androidConf
    private AndroidApphanceConfiguration androidApphanceConf

    private List<AndroidVariantConfiguration> variants

    @Inject
    AndroidVariantsConfiguration(Project project,
                                 AndroidConfiguration androidConf,
                                 AndroidApphanceConfiguration androidApphanceConf) {
        this.project = project
        this.androidConf = androidConf
        this.androidApphanceConf = androidApphanceConf
    }

    @Override
    def init() {
        super.init()
        this.variants = buildVariantsList()
    }

    def variantsNames = new ListStringProperty(
            name: 'android.variants',
            message: 'Variants'
    )

    private List<AndroidVariantConfiguration> buildVariantsList() {
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
        result
    }

    private List<AndroidVariantConfiguration> extractVariantsFromProperties() {
        variantsNames.value.collect { createVariant(it) }
    }

    private boolean variantsDirExistsAndIsNotEmpty() {
        File variantsDir = getVariantsDir()
        (variantsDir && variantsDir.isDirectory() && variantsDir.list().size() > 0)
    }

    private File getVariantsDir() {
        project.file('variants')
    }

    private List<AndroidVariantConfiguration> extractVariantsFromDir() {
        getVariantsDir().listFiles()*.name.collect { String dirName ->
            AndroidBuildMode.values()*.name().collect { String modeName ->
                createVariant(dirName.toLowerCase().capitalize() + modeName.toLowerCase().capitalize())
            }
        }.flatten()
    }

    private List<AndroidVariantConfiguration> extractDefaultVariants() {
        AndroidBuildMode.values().collect { createVariant(it.name().toLowerCase().capitalize()) }
    }

    private AndroidVariantConfiguration createVariant(String name) {
        def avc = new AndroidVariantConfiguration(name, propertyPersister, androidConf, androidApphanceConf)
        avc.init()
        avc
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
        androidConf.enabled
    }
}
