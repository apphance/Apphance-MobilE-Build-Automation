package com.apphance.flow.plugins.ios.framework

import com.apphance.flow.configuration.ios.IOSFrameworkConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.buildplugin.tasks.CopyMobileProvisionTask
import com.apphance.flow.plugins.ios.framework.tasks.IOSFrameworkBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static org.gradle.api.logging.Logging.getLogger

/**
 * Plugin for preparing reports after successful IOS build.
 *
 * This plugins provides functionality of building shared framework for IOS projects.
 *
 * While iOS itself provides a number of frameworks (shared libraries) that
 * can be used in various projects. It is undocumented feature of iOS that one can create own
 * framework. This plugin closes the gap.
 */
class IOSFrameworkPlugin implements Plugin<Project> {

    private logger = getLogger(getClass())

    @Inject IOSFrameworkConfiguration frameworkConf
    @Inject IOSVariantsConfiguration variantsConf

    @Override
    void apply(Project project) {
        if (frameworkConf.isEnabled()) {
            logger.lifecycle("Applying plugin ${this.class.simpleName}")

            def task = project.task(IOSFrameworkBuilder.NAME,
                    type: IOSFrameworkBuilder,
                    dependsOn: [CopyMobileProvisionTask.NAME]) as IOSFrameworkBuilder

            task.variant = frameworkVariant()
        }
    }

    private IOSVariant frameworkVariant() {
        variantsConf.variants.find {
            it.name == frameworkConf.variantName.value
        }
    }
}
