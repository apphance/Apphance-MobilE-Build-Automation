package com.apphance.ameba.plugins.ios.apphance

import com.apphance.ameba.plugins.ios.buildplugin.tasks.IOSAllSimulatorsBuilder
import com.apphance.ameba.plugins.project.ProjectPlugin
import com.apphance.ameba.plugins.release.tasks.ImageMontageTask
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSApphancePluginSpec extends Specification {

    def "no tasks added when no buildable variants exist"() {
        given:
        def project = builder().build()

        and:
        project.plugins.apply(ProjectPlugin)

        when:
        project.plugins.apply(IOSApphancePlugin)

        then: 'apphance configuration is added'
        project.configurations.apphance

        then: 'no build & upload tasks added'
        !project.tasks.any { it.name.startsWith('upload-') }
        !project.tasks.any { it.name.startsWith('build-') }
    }

    def "plugin tasks' graph configured correctly when buildable variants exists"() {
        given:
        def project = builder().build()

        and: 'add fake tasks'
        project.task('build-id1')
        project.task('build-id2')

        and:
        project.plugins.apply(ProjectPlugin)

        when:
        project.plugins.apply(IOSApphancePlugin)

        then: 'apphance configuration is added'
        project.configurations.apphance

        then: 'tasks for buildable variants added'
        project.tasks['upload-id1']
        project.tasks['upload-id2']

        then: 'tasks also have actions declared'
        project.tasks['build-id1'].actions
        project.tasks['build-id2'].actions
        project.tasks['upload-id1'].actions
        project.tasks['upload-id2'].actions

        then: 'no buildAllSimulators task is present'
        !project.tasks.findByName(IOSAllSimulatorsBuilder.NAME)

        then: 'each tasks has correct dependency'
        project.tasks['upload-id1'].dependsOn.containsAll('build-id1', ImageMontageTask.NAME)
        project.tasks['upload-id2'].dependsOn.containsAll('build-id2', ImageMontageTask.NAME)
    }

    def "plugin tasks' graph configured correctly when buildAllSimulators tasks exists"() {
        given:
        def project = builder().build()

        and:
        project.plugins.apply(ProjectPlugin)

        and: 'add buildAllSimulators task'
        project.task(IOSAllSimulatorsBuilder.NAME)

        expect:
        !project.tasks[IOSAllSimulatorsBuilder.NAME].actions

        when:
        project.plugins.apply(IOSApphancePlugin)

        then: 'apphance configuration is added'
        project.configurations.apphance

        then: 'tasks added'
        project.tasks[IOSAllSimulatorsBuilder.NAME]

        then: 'buildAllSmulators has actions'
        project.tasks[IOSAllSimulatorsBuilder.NAME].actions

        then: 'tasks not added'
        !project.tasks.findByName('build-id1')
        !project.tasks.findByName('build-id2')
        !project.tasks.findByName('upload-id1')
        !project.tasks.findByName('upload-id2')
    }
}
