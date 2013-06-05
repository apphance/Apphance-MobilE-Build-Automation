package com.apphance.ameba.plugins.ios.buildplugin

import com.apphance.ameba.configuration.ios.IOSConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.ameba.configuration.properties.IOSBuildModeProperty
import com.apphance.ameba.plugins.ios.buildplugin.tasks.CopyMobileProvisionTask
import com.apphance.ameba.plugins.ios.buildplugin.tasks.CopySourcesTask
import com.apphance.ameba.plugins.ios.buildplugin.tasks.UnlockKeyChainTask
import com.apphance.ameba.plugins.project.tasks.CleanFlowTask
import spock.lang.Specification

import static com.apphance.ameba.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.ameba.configuration.ios.IOSBuildMode.SIMULATOR
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.plugins.ios.buildplugin.IOSPlugin.*
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        project.task(CleanFlowTask.NAME)

        and:
        def conf = GroovyMock(IOSConfiguration)
        conf.isEnabled() >> true

        and:
        def variantsConf = GroovyMock(IOSVariantsConfiguration)
        variantsConf.variants >> [
                GroovyMock(AbstractIOSVariant, {
                    getBuildTaskName() >> 'buildV1'
                    getMode() >> new IOSBuildModeProperty(value: DEVICE)
                }
                ),
                GroovyMock(AbstractIOSVariant, {
                    getBuildTaskName() >> 'buildV2'
                    getMode() >> new IOSBuildModeProperty(value: SIMULATOR)
                }),
        ]

        and:
        def plugin = new IOSPlugin()
        plugin.conf = conf
        plugin.variantsConf = variantsConf

        when:
        plugin.apply(project)

        then:
        project.tasks[CopySourcesTask.NAME].group == AMEBA_BUILD
        project.tasks[CopyMobileProvisionTask.NAME].group == AMEBA_BUILD
        project.tasks[UnlockKeyChainTask.NAME].group == AMEBA_BUILD
        project.tasks[BUILD_ALL_DEVICE_TASK_NAME].group == AMEBA_BUILD
        project.tasks[BUILD_ALL_SIMULATOR_TASK_NAME].group == AMEBA_BUILD
        project.tasks[BUILD_ALL_TASK_NAME].group == AMEBA_BUILD
        project.tasks['buildV1'].group == AMEBA_BUILD
        project.tasks['buildV2'].group == AMEBA_BUILD

        and:
        project.tasks[BUILD_ALL_TASK_NAME].dependsOn.flatten().containsAll(BUILD_ALL_SIMULATOR_TASK_NAME, BUILD_ALL_DEVICE_TASK_NAME)
        project.tasks['buildV1'].dependsOn.flatten().contains(CopyMobileProvisionTask.NAME)
        project.tasks['buildV2'].dependsOn.flatten().contains(CopyMobileProvisionTask.NAME)
        project.tasks[BUILD_ALL_SIMULATOR_TASK_NAME].dependsOn.flatten().contains('buildV2')
        project.tasks[BUILD_ALL_DEVICE_TASK_NAME].dependsOn.flatten().contains('buildV1')
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
        !project.getTasksByName(UnlockKeyChainTask.NAME, false)
        !project.getTasksByName(CopyMobileProvisionTask.NAME, false)
        !project.getTasksByName(BUILD_ALL_TASK_NAME, false)
        !project.getTasksByName(BUILD_ALL_SIMULATOR_TASK_NAME, false)
        !project.getTasksByName(BUILD_ALL_DEVICE_TASK_NAME, false)
        !project.getTasksByName('buildV1', false)
        !project.getTasksByName('buildV2', false)
    }
}
