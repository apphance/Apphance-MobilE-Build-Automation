package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.configuration.ios.variants.IOSTCVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.apphance.flow.plugins.ios.release.IOSReleaseListener
import com.apphance.flow.plugins.project.ProjectPlugin
import com.apphance.flow.plugins.release.tasks.ImageMontageTask
import spock.lang.Ignore
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSApphancePluginSpec extends Specification {

    def "no tasks added when no buildable variants exist"() {
        given:
        def project = builder().build()

        when:
        def plugin = new IOSApphancePlugin()
        plugin.apphanceConf = GroovyStub(ApphanceConfiguration) { isEnabled() >> true }
        plugin.variantsConf = GroovyStub(IOSVariantsConfiguration) { getVariants() >> [] }
        plugin.builder = new IOSSingleVariantBuilder()
        plugin.listener = new IOSReleaseListener()
        plugin.apply(project)

        then: 'apphance configuration is added'
        project.configurations.apphance

        then: 'no build & upload tasks added'
        !project.tasks.any { it.name ==~ '(upload|build)-' }

        then:
        plugin.builder.buildListeners.size() > 0
    }

    def 'no tasks added when configuration disabled'() {
        given:
        def project = builder().build()

        when:
        def plugin = new IOSApphancePlugin()
        plugin.apphanceConf = GroovyMock(ApphanceConfiguration)
        plugin.apphanceConf.enabled >> false
        plugin.builder = new IOSSingleVariantBuilder()
        plugin.listener = new IOSReleaseListener()

        def id1 = new IOSTCVariant('id1')
        id1.configuration = 'c1'
        id1.target = 't1'
        id1.apphanceMode.value = ApphanceMode.QA.toString()
        plugin.variantsConf = GroovyStub(IOSVariantsConfiguration) { getVariants() >> [id1] }
        plugin.apply(project)

        then: 'apphance configuration is added'
        !project.configurations.contains('apphance')

        then: 'no build & upload tasks added'
        !project.tasks.any { it.name ==~ '(upload|build)-' }

        then:
        plugin.builder.buildListeners.size() == 0
    }

    def "plugin tasks' graph configured correctly when buildable variants exists"() {
        given:
        def project = builder().build()

        and: 'add fake tasks'
        project.task('buildid1')
        project.task('buildid2')

        when:
        def plugin = new IOSApphancePlugin()
        plugin.apphanceConf = GroovyStub(ApphanceConfiguration) { isEnabled() >> true }
        plugin.builder = new IOSSingleVariantBuilder()
        plugin.listener = new IOSReleaseListener()

        def id1 = new IOSTCVariant('id1')
        id1.configuration = 'c1'
        id1.target = 't1'
        id1.apphanceMode.value = ApphanceMode.QA.toString()

        def id2 = new IOSTCVariant('id2')
        id2.configuration = 'c2'
        id2.target = 't2'
        id2.apphanceMode.value = ApphanceMode.QA.toString()


        plugin.variantsConf = GroovyStub(IOSVariantsConfiguration) { getVariants() >> [id1, id2] }
        plugin.apply(project)

        then: 'apphance configuration is added'
        project.configurations.apphance

        then: 'tasks for buildable variants added'
        project.tasks['uploadid1']
        project.tasks['uploadid2']

        then: 'tasks also have actions declared'
        project.tasks['buildid1'].actions
        project.tasks['buildid2'].actions
        project.tasks['uploadid1'].actions
        project.tasks['uploadid2'].actions

        then: 'each tasks has correct dependency'
        project.tasks['uploadid1'].dependsOn.flatten().containsAll('buildid1', ImageMontageTask.NAME)
        project.tasks['uploadid2'].dependsOn.flatten().containsAll('buildid2', ImageMontageTask.NAME)

        then:
        plugin.builder.buildListeners.size() > 0
    }

    @Ignore('Verify if this specification is still up-to-date. Do we create buildAllSimulators task independently of variant configuration?')
    def "plugin tasks' graph configured correctly when buildAllSimulators tasks exists"() {
        given:
        def project = builder().build()

        and:
        project.plugins.apply(ProjectPlugin)

        when:
        def plugin = new IOSApphancePlugin()
        plugin.apphanceConf = GroovyStub(ApphanceConfiguration) { isEnabled() >> true }
        plugin.variantsConf = GroovyStub(IOSVariantsConfiguration) { getVariants() >> [] }
        plugin.apply(project)

        then: 'apphance configuration is added'
        project.configurations.apphance

        then: 'tasks not added'
        !project.tasks.findByName('build-id1')
        !project.tasks.findByName('build-id2')
        !project.tasks.findByName('upload-id1')
        !project.tasks.findByName('upload-id2')
    }
}
