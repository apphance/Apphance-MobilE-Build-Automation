package com.apphance.flow.configuration.android.variants

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.android.AndroidBuildMode
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.flow.configuration.properties.ListStringProperty.getSEPARATOR
import static com.apphance.flow.configuration.reader.GradlePropertiesPersister.FLOW_PROP_FILENAME
import static org.gradle.api.logging.Logging.getLogger

@com.google.inject.Singleton
class AndroidVariantsConfiguration extends AbstractConfiguration {

    def logger = getLogger(getClass())

    String configurationName = 'Android Variants Configuration'

    @Inject Project project
    @Inject AndroidConfiguration conf
    @Inject AndroidVariantFactory variantFactory

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
            possibleValues: { variantsNames.value ?: [] },
            validator: {
                def list = variantsNames.convert(it.toString())
                list.size() == list.unique().size() && !list.isEmpty()
            }
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
            AndroidBuildMode.values().collect { mode ->
                variantFactory.create(dirName + mode.capitalize(), new File(variantsDir, dirName))
            }
        }.flatten()
    }

    private List<AndroidVariantConfiguration> extractDefaultVariants() {
        AndroidBuildMode.values().collect { variantFactory.create(it.capitalize()) }
    }

    String getMainVariant() {
        variantsNames.value?.empty ? null : variantsNames.value[0]
    }

    @Override
    Collection<AndroidVariantConfiguration> getSubConfigurations() {
        this.getVariants()
    }

    Collection<AndroidVariantConfiguration> getVariants() {
        this.@variants.findAll { it.name in variantsNames.value }
    }

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    @Override
    void checkProperties() {
        check variantsNames.value.sort() == variants*.name.sort(), "List in '${variantsNames.name}' property is not equal to the list of names of configured variants, check '${FLOW_PROP_FILENAME}' file!"
    }
}
