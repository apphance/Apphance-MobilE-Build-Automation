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
            askUser: { false }
    )

    private List<AndroidVariantConfiguration> buildVariantsList() {
        List<AndroidVariantConfiguration> result = []
        if (variantsNames.value) {
            result.addAll(extractVariantsFromProperties())
        } else if (variantsDirExistsAndIsNotEmpty()) {
            variantsNames.value = variantsDir.listFiles().collect { it.name.toLowerCase() }.join(SEPARATOR)
            result.addAll(extractVariantsFromDir())
        } else {
            variantsNames.value = AndroidBuildMode.values().collect { it.name().toLowerCase() }.join(SEPARATOR)
            result.addAll(extractDefaultVariants())
        }
        result
    }

    private List<AndroidVariantConfiguration> extractVariantsFromProperties() {
        variantsNames.value.collect { createVariant(it.toLowerCase()) }
    }

    private boolean variantsDirExistsAndIsNotEmpty() {
        File variantsDir = getVariantsDir()
        (variantsDir && variantsDir.isDirectory() && variantsDir.list().size() > 0)
    }

    private File getVariantsDir() {
        project.file('variants')
    }

    private List<AndroidVariantConfiguration> extractVariantsFromDir() {
        getVariantsDir().listFiles().collect { createVariant(it.name.toLowerCase()) }
    }

    private List<AndroidVariantConfiguration> extractDefaultVariants() {
        AndroidBuildMode.values().collect { createVariant(it.name().toLowerCase()) }
    }

    private AndroidVariantConfiguration createVariant(String name) {
        def avc = new AndroidVariantConfiguration(name, propertyPersister, androidConf, androidApphanceConf, project)
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

    @Override
    boolean isActive() {
        androidConf.active
    }
}
