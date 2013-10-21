package com.apphance.flow.plugins.ios.buildplugin

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import com.apphance.flow.plugins.ios.buildplugin.tasks.CopyMobileProvisionTask
import com.apphance.flow.plugins.ios.buildplugin.tasks.UnlockKeyChainTask
import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import com.apphance.flow.plugins.project.tasks.CopySourcesTask
import spock.lang.Specification

import static com.apphance.flow.configuration.ios.IOSBuildMode.*
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static com.apphance.flow.plugins.ios.buildplugin.IOSPlugin.*
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        project.task(CleanFlowTask.NAME)
        project.task(CopySourcesTask.NAME)

        and:
        def conf = GroovyMock(IOSConfiguration)
        conf.isEnabled() >> true

        and:
        def variantsConf = GroovyMock(IOSVariantsConfiguration)
        variantsConf.variants >> [
                GroovyMock(AbstractIOSVariant, {
                    getArchiveTaskName() >> 'archiveV1'
                    getMode() >> new IOSBuildModeProperty(value: DEVICE)
                }
                ),
                GroovyMock(AbstractIOSVariant, {
                    getArchiveTaskName() >> 'archiveV2'
                    getMode() >> new IOSBuildModeProperty(value: SIMULATOR)
                }),
                GroovyMock(AbstractIOSVariant, {
                    getFrameworkTaskName() >> 'frameworkV3'
                    getMode() >> new IOSBuildModeProperty(value: FRAMEWORK)
                })
        ]

        and:
        def plugin = new IOSPlugin()
        plugin.conf = conf
        plugin.variantsConf = variantsConf

        when:
        plugin.apply(project)

        then:
        project.tasks[CopyMobileProvisionTask.NAME].group == FLOW_BUILD.name()
        project.tasks[UnlockKeyChainTask.NAME].group == FLOW_BUILD.name()

        and:
        project.tasks[ARCHIVE_ALL_DEVICE_TASK_NAME].group == FLOW_BUILD.name()
        project.tasks[ARCHIVE_ALL_SIMULATOR_TASK_NAME].group == FLOW_BUILD.name()
        project.tasks[ARCHIVE_ALL_TASK_NAME].group == FLOW_BUILD.name()
        project.tasks['archiveV1'].group == FLOW_BUILD.name()
        project.tasks['archiveV2'].group == FLOW_BUILD.name()

        and:
        project.tasks[FRAMEWORK_ALL].group == FLOW_BUILD.name()
        project.tasks['frameworkV3'].group == FLOW_BUILD.name()

        and:
        project.tasks[ARCHIVE_ALL_TASK_NAME].dependsOn.flatten().containsAll(ARCHIVE_ALL_SIMULATOR_TASK_NAME, ARCHIVE_ALL_DEVICE_TASK_NAME)
        project.tasks['archiveV1'].dependsOn.flatten().contains(CopyMobileProvisionTask.NAME)
        project.tasks['archiveV2'].dependsOn.flatten().contains(CopySourcesTask.NAME)
        project.tasks[ARCHIVE_ALL_SIMULATOR_TASK_NAME].dependsOn.flatten().contains('archiveV2')
        project.tasks[ARCHIVE_ALL_DEVICE_TASK_NAME].dependsOn.flatten().contains('archiveV1')

        and:
        project.tasks[FRAMEWORK_ALL].dependsOn.flatten().containsAll('frameworkV3')
        project.tasks['frameworkV3'].dependsOn.flatten().containsAll(CopySourcesTask.NAME)
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def plugin = new IOSPlugin()

        and:
        plugin.conf = GroovyMock(IOSConfiguration) {
            isEnabled() >> false
        }

        when:
        plugin.apply(project)

        then:
        !project.getTasksByName(CopySourcesTask.NAME, false)
        !project.getTasksByName(UnlockKeyChainTask.NAME, false)
        !project.getTasksByName(CopyMobileProvisionTask.NAME, false)
        !project.getTasksByName(ARCHIVE_ALL_TASK_NAME, false)
        !project.getTasksByName(ARCHIVE_ALL_SIMULATOR_TASK_NAME, false)
        !project.getTasksByName(ARCHIVE_ALL_DEVICE_TASK_NAME, false)
        !project.getTasksByName(FRAMEWORK_ALL, false)
    }
}
