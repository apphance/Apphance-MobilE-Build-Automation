package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.configuration.properties.IOSBuildModeProperty
import spock.lang.Specification

import static com.apphance.flow.configuration.apphance.ApphanceMode.DISABLED
import static com.apphance.flow.configuration.apphance.ApphanceMode.QA
import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.ios.IOSBuildMode.SIMULATOR
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSApphancePluginSpec extends Specification {

    def 'no tasks added when no buildable variants exist'() {
        given:
        def project = builder().build()

        when:
        def plugin = new IOSApphancePlugin()
        plugin.apphanceConf = GroovyStub(ApphanceConfiguration) { isEnabled() >> true }
        plugin.variantsConf = GroovyStub(IOSVariantsConfiguration) { getVariants() >> [] }
        plugin.apply(project)

        then: 'no build & upload tasks added'
        !project.tasks.any { it.name ==~ '(upload|build)-' }
    }

    def 'no tasks added when configuration disabled'() {
        given:
        def project = builder().build()

        and:
        def plugin = new IOSApphancePlugin()
        plugin.apphanceConf = GroovyMock(ApphanceConfiguration)
        plugin.apphanceConf.enabled >> false

        when:
        plugin.apply(project)

        then: 'apphance configuration is added'
        !project.configurations.contains('apphance')

        then: 'no build & upload tasks added'
        !project.tasks.any { it.name ==~ '(upload|build)' }
    }

    def "plugin tasks' graph configured correctly when buildable variants exists"() {
        given:
        def project = builder().build()

        and: 'add fake tasks'
        project.task('buildV1')
        project.task('buildV2')
        project.task('buildV3')
        project.task('buildV4')
        project.task('archiveV1')
        project.task('archiveV2')
        project.task('archiveV3')
        project.task('archiveV4')

        when:
        def plugin = new IOSApphancePlugin()
        plugin.apphanceConf = GroovyStub(ApphanceConfiguration) { isEnabled() >> true }
        plugin.variantsConf = GroovyStub(IOSVariantsConfiguration) {
            getVariants() >> [
                    GroovyMock(AbstractIOSVariant) {
                        getName() >> 'V1'
                        getApphanceMode() >> new ApphanceModeProperty(value: QA)
                        getArchiveTaskName() >> 'archiveV1'
                        getUploadTaskName() >> 'uploadV1'
                        getMode() >> new IOSBuildModeProperty(value: DEVICE)
                    },
                    GroovyMock(AbstractIOSVariant) {
                        getName() >> 'V2'
                        getApphanceMode() >> new ApphanceModeProperty(value: QA)
                        getArchiveTaskName() >> 'archiveV2'
                        getUploadTaskName() >> 'uploadV2'
                        getMode() >> new IOSBuildModeProperty(value: DEVICE)
                    },
                    GroovyMock(AbstractIOSVariant) {
                        getName() >> 'V3'
                        getApphanceMode() >> new ApphanceModeProperty(value: DISABLED)
                        getArchiveTaskName() >> 'archiveV3'
                        getUploadTaskName() >> 'uploadV3'
                        getMode() >> new IOSBuildModeProperty(value: DEVICE)
                    },
                    GroovyMock(AbstractIOSVariant) {
                        getName() >> 'V4'
                        getApphanceMode() >> new ApphanceModeProperty(value: DISABLED)
                        getArchiveTaskName() >> 'archiveV4'
                        getUploadTaskName() >> 'uploadV4'
                        getMode() >> new IOSBuildModeProperty(value: SIMULATOR)
                    }
            ]
        }
        plugin.apply(project)

        then: 'tasks for buildable variants added'
        project.tasks['uploadV1']
        project.tasks['uploadV2']
        !project.getTasksByName('uploadV3', false)
        !project.getTasksByName('uploadV4', false)

        then: 'tasks also have actions declared'
        project.tasks['archiveV1'].actions
        project.tasks['archiveV2'].actions
        !project.tasks['archiveV3'].actions
        !project.tasks['archiveV4'].actions
        project.tasks['uploadV1'].actions
        project.tasks['uploadV2'].actions
    }
}
