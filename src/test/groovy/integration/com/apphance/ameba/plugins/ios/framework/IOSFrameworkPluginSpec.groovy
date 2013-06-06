package com.apphance.ameba.plugins.ios.framework

import com.apphance.ameba.configuration.ios.IOSFrameworkConfiguration
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.plugins.ios.buildplugin.tasks.CopyMobileProvisionTask
import com.apphance.ameba.plugins.ios.framework.tasks.BuildFrameworkTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.FlowTasksGroups.FLOW_BUILD
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSFrameworkPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        def plugin = new IOSFrameworkPlugin()
        plugin.frameworkConf = GroovyMock(IOSFrameworkConfiguration, { isEnabled() >> true })
        plugin.variantsConf = GroovyMock(IOSVariantsConfiguration, { getVariants() >> [] })

        when:
        plugin.apply(project)

        then:
        project.tasks[BuildFrameworkTask.NAME].group == FLOW_BUILD.name()

        and:
        project.tasks[BuildFrameworkTask.NAME].dependsOn.flatten().containsAll(CopyMobileProvisionTask.NAME)
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def plugin = new IOSFrameworkPlugin()
        plugin.frameworkConf = Stub(IOSFrameworkConfiguration, { isEnabled() >> false })

        when:
        plugin.apply(project)

        then:
        !project.getTasksByName(BuildFrameworkTask.NAME, false)
    }
}
