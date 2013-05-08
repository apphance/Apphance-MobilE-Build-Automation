package com.apphance.ameba.plugins.ios.buildplugin

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.plugins.ios.buildplugin.tasks.*
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        def conf = GroovyMock(IOSConfiguration)
        conf.isEnabled() >> true

        and:
        def plugin = new IOSPlugin()
        plugin.conf = conf

        when:
        plugin.apply(project)

        then:
        project.tasks[CopySourcesTask.NAME].group == AMEBA_BUILD
        project.tasks[CopyDebugSourcesTask.NAME].group == AMEBA_BUILD
        project.tasks[CleanTask.NAME].group == AMEBA_BUILD
        project.tasks[UnlockKeyChainTask.NAME].group == AMEBA_BUILD
        project.tasks[CopyMobileProvisionTask.NAME].group == AMEBA_BUILD
        project.tasks[BuildSingleVariantTask.NAME].group == AMEBA_BUILD
        project.tasks[IOSAllSimulatorsBuilder.NAME].group == AMEBA_BUILD

        and:
        project.tasks[BuildSingleVariantTask.NAME].dependsOn.flatten().contains(CopySourcesTask.NAME)
        project.tasks[IOSAllSimulatorsBuilder.NAME].dependsOn.flatten().containsAll(CopyDebugSourcesTask.NAME,
                CopyMobileProvisionTask.NAME)
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def conf = GroovyMock(IOSConfiguration)
        conf.isEnabled() >> false

        and:
        def plugin = new IOSPlugin()
        plugin.conf = conf

        when:
        plugin.apply(project)

        then:
        !project.getTasksByName(CopySourcesTask.NAME, false)
        !project.getTasksByName(CopyDebugSourcesTask.NAME, false)
        !project.getTasksByName(CleanTask.NAME, false)
        !project.getTasksByName(UnlockKeyChainTask.NAME, false)
        !project.getTasksByName(CopyMobileProvisionTask.NAME, false)
        !project.getTasksByName(BuildSingleVariantTask.NAME, false)
        !project.getTasksByName(IOSAllSimulatorsBuilder.NAME, false)
    }
}
