package com.apphance.ameba.plugins.android.apphance

import com.apphance.ameba.configuration.android.AndroidApphanceConfiguration
import com.apphance.ameba.plugins.android.apphance.tasks.AndroidLogsConversionTask
import com.apphance.ameba.plugins.android.apphance.tasks.ApphanceLogsConversionTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidApphancePluginSpec extends Specification {


    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        def aap = new AndroidApphancePlugin()

        and: 'create mock android release configuration and set it'
        def aac = Mock(AndroidApphanceConfiguration)
        aac.isEnabled() >> true
        aap.apphanceConf = aac

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
        def aac = Mock(AndroidApphanceConfiguration)
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
}
