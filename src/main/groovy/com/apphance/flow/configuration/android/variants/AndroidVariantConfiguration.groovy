package com.apphance.flow.configuration.android.variants

import com.apphance.flow.configuration.android.AndroidBuildMode
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.configuration.properties.BooleanProperty
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.variants.AbstractVariant
import com.apphance.flow.util.FlowUtils
import com.google.common.io.Files
import com.google.inject.assistedinject.Assisted
import com.google.inject.assistedinject.AssistedInject

import javax.inject.Inject
import java.nio.file.Paths

import static com.apphance.flow.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.flow.configuration.android.AndroidBuildMode.RELEASE
import static com.apphance.flow.configuration.apphance.ApphanceMode.PROD
import static com.apphance.flow.util.file.FileManager.relativeTo
import static java.nio.charset.StandardCharsets.UTF_8

@Mixin(FlowUtils)
class AndroidVariantConfiguration extends AbstractVariant {

    final String prefix = 'android'
    static def APPHANCE_MAVEN = 'http://repo1.maven.org/maven2/com/utest/'

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

    def variantDir = new FileProperty(
            interactive: { false }
    )

    def oldPackage = new StringProperty(interactive: { false })
    def newPackage = new StringProperty(interactive: { false })
    def newLabel = new StringProperty(interactive: { false })
    def newName = new StringProperty(interactive: { false })
    def mergeManifest = new BooleanProperty(interactive: { false })

    @Override
    List<String> possibleApphanceLibVersions() {
        parseVersionsFromMavenMetadata(apphanceMode.value)
    }

    List<String> parseVersionsFromMavenMetadata(ApphanceMode mode) {
        try {
            def file = downloadToTempFile APPHANCE_MAVEN + "apphance-${mode == PROD ? 'prod' : 'preprod'}/maven-metadata.xml"
            def metadata = new XmlSlurper().parse(file)
            metadata.versioning.versions.version.collect { it.text() }
        } catch (Exception exp) {
            logger.warn "error during parsing apphance lib versions from maven: $exp.message"
            []
        }
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
        def file = new File(tmpDir, 'ant.properties')
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
