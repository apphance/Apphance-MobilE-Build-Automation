package com.apphance.ameba.plugins.android.apphance

import com.apphance.ameba.configuration.android.AndroidBuildMode
import com.apphance.ameba.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.plugins.android.apphance.tasks.AndroidLogsConversionTask
import com.apphance.ameba.plugins.android.apphance.tasks.ApphanceLogsConversionTask
import spock.lang.Specification

import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.ameba.configuration.android.AndroidBuildMode.RELEASE
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
import static com.apphance.ameba.plugins.android.buildplugin.AndroidPlugin.BUILD_ALL_DEBUG_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidApphancePluginSpec extends Specification {


    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        def aap = new AndroidApphancePlugin()

        and: 'create mock android apphance configuration and set it'
        def aac = Mock(ApphanceConfiguration)
        aac.isEnabled() >> true
        aap.apphanceConf = aac

        and: 'create mock android variants configuration and set it'
        aap.variantsConf = Mock(AndroidVariantsConfiguration)

        when:
        aap.apply(project)

        then: 'apphance configuration was added'
        project.configurations.apphance

        then: 'each task has correct group'
        project.tasks[ApphanceLogsConversionTask.NAME].group == AMEBA_APPHANCE_SERVICE
        project.tasks[AndroidLogsConversionTask.NAME].group == AMEBA_APPHANCE_SERVICE
    }

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

        then:
        !project.getTasksByName(ApphanceLogsConversionTask.NAME, false)
        !project.getTasksByName(AndroidLogsConversionTask.NAME, false)
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
                createVariant('v1', DEBUG),
                createVariant('v2', RELEASE),
                createVariant('v3', DEBUG),
        ]
        aap.variantsConf = avc

        and: 'add tasks that AndroidPlugin creates'
        project.task('buildv1')
        project.task('buildv2')
        project.task('buildv3')

        when:
        aap.apply(project)

        then: 'apphance configuration was added'
        project.configurations.apphance

        then: 'each task has correct group'
        project.tasks[ApphanceLogsConversionTask.NAME].group == AMEBA_APPHANCE_SERVICE
        project.tasks[AndroidLogsConversionTask.NAME].group == AMEBA_APPHANCE_SERVICE

        and: 'apphance tasks defined'
        project.tasks['uploadv1'].dependsOn.flatten().contains('buildv1')
        !project.tasks.findByName('uploadv2')
        project.tasks['uploadv3'].dependsOn.flatten().contains('buildv3')
    }

    private AndroidVariantConfiguration createVariant(String name, AndroidBuildMode mode) {
        def avc = GroovyMock(AndroidVariantConfiguration)
        avc.name >> name
        avc.mode >> mode
        avc
    }
}
