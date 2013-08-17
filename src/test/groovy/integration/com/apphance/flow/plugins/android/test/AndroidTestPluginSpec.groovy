package com.apphance.flow.plugins.android.test

import com.apphance.flow.configuration.android.AndroidTestConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidTestPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        def plugin = new AndroidTestPlugin()

        and:
        def atc = GroovyStub(AndroidTestConfiguration)
        atc.isEnabled() >> true
        plugin.testConf = atc

        and:
        def variantsConf = GroovyStub(AndroidVariantsConfiguration)
        variantsConf.variants >> [
                GroovyStub(AndroidVariantConfiguration) { getName() >> 'release' },
                GroovyStub(AndroidVariantConfiguration) { getName() >> 'debug' }
        ]
        plugin.variantsConf = variantsConf

        when:
        plugin.apply(project)

        then: 'test tasks added'
        project.tasks['testAll']
        project.tasks['testRelease']
        project.tasks['testDebug']

        then: 'every task has correct dependencies'
        project.testAll.dependsOn.flatten()*.toString().containsAll(['testRelease', 'testDebug'])
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def atp = new AndroidTestPlugin()

        and:
        def atc = Mock(AndroidTestConfiguration)
        atc.isEnabled() >> false
        atp.testConf = atc

        when:
        atp.apply(project)

        then:
        !project.tasks.findByName('testAll')
        !project.tasks.findByName('testRelease')
        !project.tasks.findByName('testDebug')

        then:
        !project.configurations.findByName('robotium')
        !project.configurations.findByName('robolectric')
    }
}
