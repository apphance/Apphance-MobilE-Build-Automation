package com.apphance.ameba.plugins.ios.framework

import com.apphance.ameba.configuration.ios.IOSFrameworkConfiguration
import com.apphance.ameba.plugins.ios.framework.tasks.BuildFrameworkTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.plugins.ios.buildplugin.IOSPlugin.COPY_MOBILE_PROVISION_TASK_NAME
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSFrameworkPluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        and:
        def plugin = new IOSFrameworkPlugin()
        plugin.iosFrameworkConf = Stub(IOSFrameworkConfiguration, { isEnabled() >> true })

        when:
        plugin.apply(project)

        then: 'every single task is in correct group'
        project.tasks[BuildFrameworkTask.NAME].group == AMEBA_BUILD

        and: 'task dependencies configured correctly'
        project.tasks[BuildFrameworkTask.NAME].dependsOn.flatten().containsAll(READ_PROJECT_CONFIGURATION_TASK_NAME, COPY_MOBILE_PROVISION_TASK_NAME)
    }
}
