package com.apphance.flow.plugins.ios.test

import com.apphance.flow.configuration.ios.IOSTestConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.plugins.ios.buildplugin.tasks.CopySourcesTask
import com.apphance.flow.plugins.project.tasks.VerifySetupTask
import spock.lang.Specification

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST
import static com.apphance.flow.plugins.ios.test.IOSTestPlugin.TEST_ALL_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSTestPluginSpec extends Specification {

    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()
        def plugin = new IOSTestPlugin()
        plugin.testConf = GroovyMock(IOSTestConfiguration) {
            isEnabled() >> true
            getTestVariants() >> [
                    GroovyMock(IOSVariant) {
                        getTestTaskName() >> 'testV1'
                        getArchiveTaskName() >> 'archiveV1'
                    },
                    GroovyMock(IOSVariant) {
                        getTestTaskName() >> 'testV2'
                        getArchiveTaskName() >> 'archiveV2'
                    }
            ]
        }

        and:
        project.task('archiveV1')
        project.task('archiveV2')

        when:
        plugin.apply(project)

        then:
        project.tasks['testV1'].group == FLOW_TEST.name()
        project.tasks['testV2'].group == FLOW_TEST.name()
        project.tasks[TEST_ALL_TASK_NAME].group == FLOW_TEST.name()

        and:
        project.tasks['testV1'].dependsOn.flatten().containsAll(CopySourcesTask.NAME)
        project.tasks['testV2'].dependsOn.flatten().containsAll(CopySourcesTask.NAME)

        and:
        project.tasks['testV1'].getMustRunAfter().find { it.values.contains(VerifySetupTask.NAME) }
        project.tasks['testV2'].getMustRunAfter().find { it.values.contains(VerifySetupTask.NAME) }

        and:
        project.tasks['archiveV1'].dependsOn.flatten().containsAll('testV1')
        project.tasks['archiveV2'].dependsOn.flatten().containsAll('testV2')

        and:
        project.tasks[TEST_ALL_TASK_NAME].dependsOn.flatten().containsAll('testV1', 'testV2')
        project.tasks[TEST_ALL_TASK_NAME].getMustRunAfter().find { it.values.contains(VerifySetupTask.NAME) }
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def conf = Stub(IOSTestConfiguration)
        conf.isEnabled() >> false

        and:
        def plugin = new IOSTestPlugin()
        plugin.testConf = conf

        when:
        plugin.apply(project)

        then:
        !project.getTasksByName(TEST_ALL_TASK_NAME, false)
    }

    def 'no tasks available when configuration is active but no test variants passed'() {
        given:
        def project = builder().build()

        and:
        def conf = GroovyMock(IOSTestConfiguration)
        conf.isEnabled() >> true
        conf.getTestVariants() >> []

        and:
        def plugin = new IOSTestPlugin()
        plugin.testConf = conf

        when:
        plugin.apply(project)

        then:
        !project.getTasksByName(TEST_ALL_TASK_NAME, false)
    }
}
