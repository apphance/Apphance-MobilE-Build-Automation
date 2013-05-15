package com.apphance.ameba.plugins.ios.buildplugin

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.plugins.ios.buildplugin.tasks.*
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.plugins.ios.buildplugin.IOSPlugin.BUILD_ALL_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        def conf = GroovyMock(IOSConfiguration)
        conf.isEnabled() >> true

        and:
        def variantsConf = GroovyMock(IOSVariantsConfiguration)
        variantsConf.variants >> [
                GroovyMock(AbstractIOSVariant, { getBuildTaskName() >> "buildV1" }),
                GroovyMock(AbstractIOSVariant, { getBuildTaskName() >> "buildV2" }),
        ]

        and:
        def plugin = new IOSPlugin()
        plugin.conf = conf
        plugin.variantsConf = variantsConf

        when:
        plugin.apply(project)

        then:
        project.tasks[CopySourcesTask.NAME].group == AMEBA_BUILD
        project.tasks[CopyDebugSourcesTask.NAME].group == AMEBA_BUILD
        project.tasks[CleanTask.NAME].group == AMEBA_BUILD
        project.tasks[UnlockKeyChainTask.NAME].group == AMEBA_BUILD
        project.tasks[CopyMobileProvisionTask.NAME].group == AMEBA_BUILD
        project.tasks[IOSAllSimulatorsBuilder.NAME].group == AMEBA_BUILD
        project.tasks['buildV1'].group == AMEBA_BUILD
        project.tasks['buildV2'].group == AMEBA_BUILD

        and:
        project.tasks[IOSAllSimulatorsBuilder.NAME].dependsOn.flatten().containsAll(CopyDebugSourcesTask.NAME,
                CopyMobileProvisionTask.NAME)
        project.tasks[BUILD_ALL_TASK_NAME].dependsOn.flatten().containsAll('buildV1', 'buildV2')
        project.tasks['buildV1'].dependsOn.flatten().containsAll(CopySourcesTask.NAME, CopyMobileProvisionTask.NAME)
        project.tasks['buildV2'].dependsOn.flatten().containsAll(CopySourcesTask.NAME, CopyMobileProvisionTask.NAME)
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def conf = GroovyMock(IOSConfiguration)
        conf.isEnabled() >> false

        and:
        def variantsConf = GroovyMock(IOSVariantsConfiguration)
        variantsConf.variants >> [
                GroovyMock(AbstractIOSVariant, { getBuildTaskName() >> "buildV1" }),
                GroovyMock(AbstractIOSVariant, { getBuildTaskName() >> "buildV2" }),
        ]

        and:
        def plugin = new IOSPlugin()
        plugin.conf = conf
        plugin.variantsConf = variantsConf

        when:
        plugin.apply(project)

        then:
        !project.getTasksByName(CopySourcesTask.NAME, false)
        !project.getTasksByName(CopyDebugSourcesTask.NAME, false)
        !project.getTasksByName(CleanTask.NAME, false)
        !project.getTasksByName(UnlockKeyChainTask.NAME, false)
        !project.getTasksByName(CopyMobileProvisionTask.NAME, false)
        !project.getTasksByName(IOSAllSimulatorsBuilder.NAME, false)
        !project.getTasksByName(BUILD_ALL_TASK_NAME, false)
        !project.getTasksByName('buildV1', false)
        !project.getTasksByName('buildV2', false)
    }
}
