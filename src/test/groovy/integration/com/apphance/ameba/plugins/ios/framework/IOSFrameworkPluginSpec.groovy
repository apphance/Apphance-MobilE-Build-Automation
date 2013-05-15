package com.apphance.ameba.plugins.ios.framework

import com.apphance.ameba.configuration.ios.IOSFrameworkConfiguration
import com.apphance.ameba.plugins.ios.buildplugin.tasks.CopyMobileProvisionTask
import com.apphance.ameba.plugins.ios.framework.tasks.BuildFrameworkTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSFrameworkPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        def plugin = new IOSFrameworkPlugin()
        plugin.frameworkConf = Stub(IOSFrameworkConfiguration, { isEnabled() >> true })

        when:
        plugin.apply(project)

        then:
        project.tasks[BuildFrameworkTask.NAME].group == AMEBA_BUILD

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
