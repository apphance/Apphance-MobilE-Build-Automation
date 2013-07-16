package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.plugins.ios.parsers.XCSchemeParser
import com.google.inject.Singleton
import groovy.transform.PackageScope

import javax.inject.Inject

import static com.google.common.base.Preconditions.checkNotNull
import static java.io.File.separator

@Singleton
class IOSVariantsConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Variants Configuration'

    @Inject IOSConfiguration conf
    @Inject IOSVariantFactory variantFactory
    @Inject XCSchemeParser schemeParser

    @Inject
    @Override
    void init() {
        super.init()
    }

    def variantsNames = new ListStringProperty(
            name: 'ios.variants',
            message: "Variants (first variant on the list will be considered as a 'main'",
            possibleValues: { possibleVariants },
            validator: {
                def list = variantsNames.convert(it.toString())
                list.size() == list.unique().size() && !list.isEmpty()
            }
    )

    @Lazy
    @PackageScope
    List<String> possibleVariants = {
        if (hasSchemes)
            return schemeFiles.findAll {
                schemeShared(it) && schemeBuildable(it) && schemeHasSingleBuildableTarget(it)
            }.collect { getNameWithoutExtension(it.name) }
        []
    }()

    private List<IOSVariant> variantsInternal() {
        variantsNames.value.collect {
            schemeVariant.call(it)
        }
    }

    @PackageScope
    Closure<IOSVariant> schemeVariant = { String name ->
        variantFactory.createSchemeVariant(name)
    }.memoize()

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    @Override
    Collection<IOSVariant> getSubConfigurations() {
        variantsInternal()
    }

    Collection<IOSVariant> getVariants() {
        variantsInternal()
    }

    IOSVariant getMainVariant() {
        variantsInternal()[0]
    }

    @Override
    boolean canBeEnabled() {
        hasSchemes
    }

    @Lazy
    @PackageScope
    boolean hasSchemes = {
        schemesDeclared() && schemesShared() && schemesBuildable() && schemesHasSingleBuildableTarget()
    }()

    @PackageScope
    boolean schemesDeclared() {
        conf.schemes.size() > 0
    }

    @PackageScope
    boolean schemesShared() {
        schemeFiles.any(this.&schemeShared)
    }

    @PackageScope
    boolean schemeShared(File scheme) {
        scheme.exists() && scheme.isFile() && scheme.size() > 0
    }

    @PackageScope
    boolean schemesBuildable() {
        schemeFiles.any(this.&schemeBuildable)
    }

    @PackageScope
    boolean schemeBuildable(File scheme) {
        schemeParser.isBuildable(scheme)
    }

    @PackageScope
    boolean schemesHasSingleBuildableTarget() {
        schemeFiles.any(this.&schemeHasSingleBuildableTarget)
    }

    @PackageScope
    boolean schemeHasSingleBuildableTarget(File scheme) {
        schemeParser.hasSingleBuildableTarget(scheme)
    }

    @PackageScope
    @Lazy
    List<File> schemeFiles = {
        conf.schemes.collect { name -> schemeFile.call(name) }
    }()

    @PackageScope
    Closure<File> schemeFile = { String name ->
        new File(conf.xcodeDir.value, "xcshareddata${separator}xcschemes$separator${name}.xcscheme")
    }.memoize()

//        """To enable '${configurationName}' project:
//           - must have shared schemes
//           - schemes must be buildable (Executable set in scheme's 'Run Action')
//           - schemes must have single buildable target
//        """

    //gradle guava 11 workaround
    @PackageScope
    String getNameWithoutExtension(String file) {
        checkNotNull(file)
        String fileName = new File(file).getName()
        int dotIndex = fileName.lastIndexOf('.')
        (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex)
    }

    @Override
    void checkProperties() {
        super.checkProperties()
    }
}
