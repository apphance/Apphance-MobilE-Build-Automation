package com.apphance.flow.configuration.ios.variants

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.properties.ListStringProperty
import com.apphance.flow.plugins.ios.scheme.IOSSchemeInfo
import com.apphance.flow.plugins.ios.workspace.IOSWorkspaceLocator
import com.apphance.flow.util.FlowUtils
import com.google.inject.Singleton
import groovy.transform.PackageScope
import org.gradle.api.GradleException

import javax.inject.Inject

@Singleton
@Mixin(FlowUtils)
class IOSVariantsConfiguration extends AbstractConfiguration {

    String configurationName = 'iOS Variants Configuration'

    @Inject IOSConfiguration conf
    @Inject IOSVariantFactory variantFactory
    @Inject IOSSchemeInfo schemeInfo
    @Inject IOSWorkspaceLocator workspaceLocator

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
                list.size() == list.unique().size() && !list.isEmpty() && list.every { it in possibleVariants }
            }
    )

    @Lazy
    @PackageScope
    List<String> possibleVariants = {
        hasWorkspaceAndSchemes ? workspaceXscheme.collect { w, s -> "$w$s".toString() } : (hasSchemes ? schemes : [])
    }()

    @Lazy
    List<List<String>> workspaceXscheme = {
        [workspaces, schemes].combinations()
    }()

    @Lazy
    private List<String> schemes = {
        schemeInfo.schemeFiles.findAll(schemeInfo.&schemeShared).collect(nameWithoutExtension)
    }()

    @Lazy
    private List<String> workspaces = {
        workspaceLocator.workspaces.collect(nameWithoutExtension)
    }()

    @Override
    boolean isEnabled() {
        conf.enabled
    }

    @Override
    Collection<? extends AbstractIOSVariant> getSubConfigurations() {
        variantsInternal()
    }

    Collection<? extends AbstractIOSVariant> getVariants() {
        variantsInternal().findAll { it.isEnabled() }
    }

    AbstractIOSVariant getMainVariant() {
        variantsInternal()[0]
    }

    private List<? extends AbstractIOSVariant> variantsInternal() {
        if (hasWorkspaceAndSchemes)
            return variantsNames.value.collect(workspaceVariant)
        else if (hasSchemes)
            return variantsNames.value.collect(schemeVariant)
        throw new GradleException('Project has no workspaces nor schemes')
    }

    @Lazy
    private boolean hasWorkspaceAndSchemes = {
        workspaceLocator.hasWorkspaces && hasSchemes
    }()

    @Lazy
    private boolean hasSchemes = {
        schemeInfo.hasSchemes
    }()

    private Closure<IOSSchemeVariant> schemeVariant = { String name ->
        variantFactory.createSchemeVariant(name)
    }.memoize()

    private Closure<IOSWorkspaceVariant> workspaceVariant = { String name ->
        variantFactory.createWorkspaceVariant(name)
    }.memoize()

    @Override
    void checkProperties() {
        super.checkProperties()
    }
}
