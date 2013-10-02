package com.apphance.flow.configuration.android.variants

import com.apphance.flow.configuration.android.AndroidBuildMode
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.properties.BooleanProperty
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.variants.AbstractVariant
import com.apphance.flow.util.FlowUtils
import com.google.inject.assistedinject.Assisted
import com.google.inject.assistedinject.AssistedInject

import javax.inject.Inject
import java.nio.file.Paths

import static com.apphance.flow.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.flow.configuration.android.AndroidBuildMode.RELEASE
import static com.apphance.flow.util.file.FileManager.asProperties
import static com.apphance.flow.util.file.FileManager.relativeTo

@Mixin(FlowUtils)
class AndroidVariantConfiguration extends AbstractVariant {

    final String prefix = 'android'

    @Inject AndroidReleaseConfiguration androidReleaseConf
    private File vDir

    @AssistedInject
    AndroidVariantConfiguration(@Assisted String name) {
        super(name.capitalize())
    }

    @AssistedInject
    AndroidVariantConfiguration(@Assisted String name, @Assisted File variantDir) {
        super(name.capitalize())
        this.vDir = variantDir
    }

    @Override
    @Inject
    void init() {
        variantDir.name = "android.variant.${name}.dir"
        oldPackage.name = "${prefix}.variant.${name}.replacePackage.oldPackage"
        newPackage.name = "${prefix}.variant.${name}.replacePackage.newPackage"
        newLabel.name = "${prefix}.variant.${name}.replacePackage.newLabel"
        newName.name = "${prefix}.variant.${name}.replacePackage.newName"
        mergeManifest.name = "${prefix}.variant.${name}.mergeManifest"

        super.init()

        if (!variantDir.value && vDir)
            variantDir.value = relativeTo(conf.rootDir.absolutePath, vDir.absolutePath)
    }

    AndroidBuildMode getMode() {
        name.toLowerCase().contains(DEBUG.lowerCase()) ? DEBUG : RELEASE
    }

    def variantDir = new FileProperty(interactive: { false })
    def oldPackage = new StringProperty(interactive: { false })
    def newPackage = new StringProperty(interactive: { false })
    def newLabel = new StringProperty(interactive: { false })
    def newName = new StringProperty(interactive: { false })
    def mergeManifest = new BooleanProperty(interactive: { false })

    @Lazy
    List<String> possibleApphanceLibVersions = {
        apphanceArtifactory.androidLibraries(apphanceMode.value)
    }()

    String getBuildTaskName() {
        "build$name".replaceAll('\\s', '')
    }

    @Override
    String getConfigurationName() {
        "Android Variant ${name}"
    }

    @Override
    void checkProperties() {
        super.checkProperties()
        if (androidReleaseConf.enabled || apphanceConf.enabled) {
            checkSigningConfiguration()
        }
    }

    void checkSigningConfiguration() {
        def signParams = ['key.store', 'key.store.password', 'key.alias', 'key.alias.password']
        def nonemptySigningProperties = signParams.collect { System.getProperty(it) }.grep()

        def file = new File(tmpDir, 'ant.properties')
        check file.exists() || nonemptySigningProperties.size() == 4, "If release or apphance plugin is enabled ant.properties should be present in " +
                "${tmpDir.absolutePath} or appropriate signing parameters ($signParams) passed to gradle as command line options"

        Properties antProperties = null
        if (file.exists()) {
            antProperties = asProperties(file)
        }
        String keyStorePath = validate(antProperties, 'key.store')
        if (keyStorePath) {
            def keyStore = Paths.get(tmpDir.absolutePath).resolve(keyStorePath).toFile()
            check keyStore?.exists(), "Keystore path is not correctly configured: File ${keyStore?.absolutePath} doesn't exist."
        }

        validate(antProperties, 'key.store.password')
        validate(antProperties, 'key.alias')
        validate(antProperties, 'key.alias.password')
    }

    String validate(Properties antProperties, String property) {
        String value = antProperties?.getProperty(property) ?: System.getProperty(property)
        check value, "$property value is not correctly configured: ${value}"
        value
    }

    File getOriginalFile() {
        new File(tmpDir, isLibrary() ? 'bin/classes.jar' : "bin/${conf.projectNameNoWhiteSpace}-${mode.lowerCase()}.apk")
    }

    Boolean isLibrary() {
        def props = new File(tmpDir, 'project.properties')
        props.exists() && asProperties(props).getProperty('android.library') == 'true'
    }

    File getBuildDir() {
        new File(tmpDir, 'bin')
    }
}
