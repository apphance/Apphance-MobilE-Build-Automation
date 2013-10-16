package com.apphance.flow.configuration.android.variants

import com.apphance.flow.configuration.android.AndroidBuildMode
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.properties.BooleanProperty
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.variants.AbstractVariant
import com.apphance.flow.detection.project.ProjectType
import com.apphance.flow.util.FlowUtils
import com.google.inject.assistedinject.Assisted
import com.google.inject.assistedinject.AssistedInject

import javax.inject.Inject
import java.nio.file.Paths

import static com.apphance.flow.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.flow.configuration.android.AndroidBuildMode.RELEASE
import static com.apphance.flow.configuration.properties.BooleanProperty.POSSIBLE_BOOLEAN
import static com.apphance.flow.detection.project.ProjectType.ANDROID
import static com.apphance.flow.util.file.FileManager.asProperties
import static com.apphance.flow.util.file.FileManager.relativeTo
import static java.text.MessageFormat.format
import static org.apache.commons.lang.StringUtils.isEmpty

@Mixin(FlowUtils)
class AndroidVariantConfiguration extends AbstractVariant {

    final ProjectType projectType = ANDROID

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
        oldPackage.name = "android.variant.${name}.replacePackage.oldPackage"
        newPackage.name = "android.variant.${name}.replacePackage.newPackage"
        newLabel.name = "android.variant.${name}.replacePackage.newLabel"
        newName.name = "android.variant.${name}.replacePackage.newName"
        mergeManifest.name = "android.variant.${name}.mergeManifest"

        super.init()

        if (!variantDir.value && vDir)
            variantDir.value = relativeTo(conf.rootDir.absolutePath, vDir.absolutePath)
    }

    AndroidBuildMode getMode() {
        name.toLowerCase().contains(DEBUG.lowerCase()) ? DEBUG : RELEASE
    }

    def variantDir = new FileProperty(
            interactive: { false },
            doc: { "Variant directory. Content of this directory overrides content of the main project directory." }
    )
    def oldPackage = new StringProperty(
            interactive: { false },
            doc: { "Package name used in main sources before package replacement in variant." }
    )
    def newPackage = new StringProperty(
            interactive: { false },
            doc: { "New package name that will be used in variant." }
    )
    def newLabel = new StringProperty(
            interactive: { false },
            doc: { "Variant value of 'android:label' attribute in manifest 'application' tag." }
    )

    def newName = new StringProperty(
            interactive: { false },
            doc: { "Variant value of 'name' attribute of build.xml." }
    )
    def mergeManifest = new BooleanProperty(
            interactive: { false },
            doc: {
                "If true then manifest file in variant directory will be merged with main project manifest. " +
                        "When false variant manifest will override main manifest. Default value: false"
            },
            validator: { isEmpty(it) ? true : it in POSSIBLE_BOOLEAN },
            possibleValues: { POSSIBLE_BOOLEAN }
    )

    @Lazy
    List<String> possibleApphanceLibVersions = {
        apphanceArtifactory.androidLibraries(aphMode.value)
    }()

    String getBuildTaskName() {
        "build$name".replaceAll('\\s', '')
    }

    @Override
    String getConfigurationName() {
        "Android Variant ${name}"
    }

    @Override
    void validate(List<String> errors) {
        super.validate(errors)
        if (androidReleaseConf.enabled || apphanceConf.enabled) {
            checkSigningConfiguration(errors)
        }
    }

    void checkSigningConfiguration(List<String> errors) {
        def signParams = ['key.store', 'key.store.password', 'key.alias', 'key.alias.password']
        def nonemptySigningProperties = signParams.collect { System.getProperty(it) }.grep()

        def file = tmpDir.exists() ? new File(tmpDir, 'ant.properties') : new File(conf.rootDir, 'ant.properties')
        errors << propValidator.validateCondition(file.exists() || nonemptySigningProperties.size() == 4,
                format(validationBundle.getString('exception.android.ant.properties'), tmpDir.absolutePath, signParams))


        Properties antProperties = null
        if (file.exists()) {
            antProperties = asProperties(file)
        }
        String keyStorePath = validate(errors, antProperties, 'key.store')
        if (keyStorePath) {
            def keyStore = Paths.get((tmpDir.exists() ? tmpDir : conf.rootDir).absolutePath).resolve(keyStorePath).toFile()
            errors << propValidator.validateCondition(keyStore?.exists(),
                    format(validationBundle.getString('exception.android.ant.keystore'), keyStore?.absolutePath))
        }

        validate(errors, antProperties, 'key.store.password')
        validate(errors, antProperties, 'key.alias')
        validate(errors, antProperties, 'key.alias.password')
    }

    String validate(List<String> errors, Properties antProperties, String property) {
        String value = antProperties?.getProperty(property) ?: System.getProperty(property)
        errors << propValidator.validateCondition(value as boolean, "$property value is not correctly configured: ${value}")
        value
    }

    File getOriginalFile() {
        new File(tmpDir, isLibrary() ? 'bin/classes.jar' : "bin/${projectNameNoWhiteSpace()}-${mode.lowerCase()}.apk")
    }

    Closure<String> projectNameNoWhiteSpace = { conf.projectNameNoWhiteSpace }

    Boolean isLibrary() {
        def props = new File(tmpDir, 'project.properties')
        props.exists() && asProperties(props).getProperty('android.library') == 'true'
    }

    File getBuildDir() {
        new File(tmpDir, 'bin')
    }
}
