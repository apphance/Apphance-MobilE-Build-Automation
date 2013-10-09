package com.apphance.flow.plugins.android.apphance

import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.configuration.properties.ApphanceModeProperty
import com.apphance.flow.plugins.android.buildplugin.tasks.CopySourcesTask
import spock.lang.Specification

import static com.apphance.flow.configuration.apphance.ApphanceMode.*
import static com.apphance.flow.plugins.android.buildplugin.AndroidPlugin.BUILD_ALL_DEBUG_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidApphancePluginSpec extends Specification {

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def aap = new AndroidApphancePlugin()

        and: 'create mock android apphance configuration and set it'
        def aac = Mock(ApphanceConfiguration)
        aac.isEnabled() >> false
        aap.apphanceConf = aac

        when:
        aap.apply(project)

        then:
        !project.configurations.findByName('apphance')

    }

    def 'tasks & variants defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()
        project.task(BUILD_ALL_DEBUG_TASK_NAME)

        and:
        def aap = new AndroidApphancePlugin()

        and: 'create mock android apphance configuration and set it'
        def aac = Mock(ApphanceConfiguration)
        aac.isEnabled() >> true
        aap.apphanceConf = aac

        and: 'create mock android variants configuration and set it'
        def avc = GroovyMock(AndroidVariantsConfiguration)
        avc.variants >> [
                createVariant('v1', QA),
                createVariant('v2', DISABLED),
                createVariant('v3', SILENT),
        ]
        aap.variantsConf = avc

        and: 'add required tasks'
        project.task('buildV1')
        project.task('buildV2')
        project.task('buildV3')

        when:
        aap.apply(project)

        then: 'apphance tasks defined'
        project.tasks['uploadV1'].dependsOn.flatten().contains('buildV1')
        !project.tasks.findByName('uploadV2')
        project.tasks['uploadV3'].dependsOn.flatten().contains('buildV3')
        project.tasks.findByName('addApphanceV1').dependsOn.flatten().contains(CopySourcesTask.NAME)
        !project.tasks.findByName('addApphanceV2')
        project.tasks.findByName('addApphanceV3').dependsOn.flatten().contains(CopySourcesTask.NAME)
    }

    private AndroidVariantConfiguration createVariant(String name, ApphanceMode mode) {
        def avc = GroovySpy(AndroidVariantConfiguration, constructorArgs: [name])
        avc.apphanceMode >> new ApphanceModeProperty(value: mode)
        avc
    }
}
