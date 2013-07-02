package com.apphance.flow.configuration.android.variants

import com.apphance.flow.configuration.android.AndroidBuildMode
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.variants.AbstractVariant
import com.google.common.io.Files
import com.google.inject.assistedinject.Assisted
import com.google.inject.assistedinject.AssistedInject

import javax.inject.Inject
import java.nio.file.Paths

import static com.apphance.flow.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.flow.configuration.android.AndroidBuildMode.RELEASE
import static com.apphance.flow.util.file.FileManager.relativeTo
import static java.nio.charset.StandardCharsets.UTF_8

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
        super.init()

        if (!variantDir.value && vDir)
            variantDir.value = relativeTo(conf.rootDir.absolutePath, vDir.absolutePath)
    }

    AndroidBuildMode getMode() {
        name.toLowerCase().contains(DEBUG.lowerCase()) ? DEBUG : RELEASE
    }

    def variantDir = new FileProperty(
            interactive: { false },
            //validator: { it in releaseConf.findMobileProvisionFiles()*.name }TODO
    )

    def oldPackage = new StringProperty(
            name: "android.variant.${name}.replacePackage.oldPackage",
            interactive: { false })
    def newPackage = new StringProperty(
            name: "android.variant.${name}.replacePackage.newPackage",
            interactive: { false })
    def newLabel = new StringProperty(
            name: "android.variant.${name}.replacePackage.newLabel",
            interactive: { false })
    def newName = new StringProperty(
            name: "android.variant.${name}.replacePackage.newName",
            interactive: { false })

    def variantIcon = new FileProperty(
            name: "android.variant.${name}.replacePackage.variantIcon",
            interactive: { false })

    @Override
    List<String> possibleApphanceLibVersions() {
        apphanceArtifactory.androidLibraries(apphanceMode.value)
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
        def file = new File(tmpDir, 'ant.properties')//project.file('ant.properties')
        check file.exists(), "If release or apphance plugin is enabled ant.properties should be present in ${tmpDir.absolutePath}"

        if (file.exists()) {
            Properties antProperties = new Properties()
            antProperties.load(Files.newReader(file, UTF_8))
            String keyStorePath = antProperties.getProperty('key.store')
            check keyStorePath, "key.store value in ant.properties file is not correctly configured: ${keyStorePath}"
            def keyStore = Paths.get(tmpDir.absolutePath).resolve(keyStorePath).toFile()
            check keyStore.exists(), "Keystore path is not correctly configured: File ${keyStore.absolutePath} doesn't exist."
        }
    }
}
