package com.apphance.flow.plugins.android.buildplugin

import com.apphance.flow.configuration.android.AndroidBuildMode
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.plugins.android.buildplugin.tasks.UpdateProjectTask
import com.apphance.flow.plugins.project.tasks.CleanFlowTask
import com.apphance.flow.plugins.project.tasks.CopySourcesTask
import org.gradle.api.plugins.JavaPlugin
import spock.lang.Specification

import static com.apphance.flow.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.flow.configuration.android.AndroidBuildMode.RELEASE
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static com.apphance.flow.plugins.android.buildplugin.AndroidPlugin.*
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        project.task(CleanFlowTask.NAME)
        project.task(CopySourcesTask.NAME)

        and:
        def ap = new AndroidPlugin()

        and: 'prepare mock configuration'
        def ac = GroovyStub(AndroidConfiguration)
        ac.isEnabled() >> true
        ap.conf = ac

        and:
        def avc = GroovyStub(AndroidVariantsConfiguration)
        avc.variants >> []
        ap.variantsConf = avc

        when:
        ap.apply(project)

        then:
        project.tasks[UpdateProjectTask.NAME].group == FLOW_BUILD.name()
        project.tasks[BUILD_ALL_TASK_NAME].group == FLOW_BUILD.name()
        project.tasks[BUILD_ALL_DEBUG_TASK_NAME].group == FLOW_BUILD.name()
        project.tasks[BUILD_ALL_RELEASE_TASK_NAME].group == FLOW_BUILD.name()

        and:
        project.tasks[BUILD_ALL_TASK_NAME].dependsOn.flatten().containsAll(BUILD_ALL_RELEASE_TASK_NAME, BUILD_ALL_DEBUG_TASK_NAME)
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def ap = new AndroidPlugin()

        and: 'prepare mock configuration'
        def ac = Mock(AndroidConfiguration)
        ac.isEnabled() >> false
        ap.conf = ac

        when:
        ap.apply(project)

        then:
        !project.plugins.findPlugin(JavaPlugin)

        then:
        !project.getTasksByName(CopySourcesTask.NAME, false)
        !project.getTasksByName(UpdateProjectTask.NAME, false)
    }

    def 'tasks & variants defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        project.task(CleanFlowTask.NAME)
        project.task(CopySourcesTask.NAME)

        and:
        def ap = new AndroidPlugin()

        and: 'prepare mock configuration'
        def ac = GroovyStub(AndroidConfiguration)
        ac.isEnabled() >> true
        ap.conf = ac

        and:
        def avc = GroovyStub(AndroidVariantsConfiguration)
        avc.variants >> [
                createVariant('v1', DEBUG),
                createVariant('v2', RELEASE)
        ]
        ap.variantsConf = avc

        when:
        ap.apply(project)

        then:
        project.tasks[UpdateProjectTask.NAME].group == FLOW_BUILD.name()

        and:
        project.tasks['buildv1']
        project.tasks['buildv2']

        and:
        project.tasks['buildv1'].dependsOn.flatten().containsAll(CopySourcesTask.NAME)
        project.tasks['buildv2'].dependsOn.flatten().containsAll(CopySourcesTask.NAME)
        project.tasks[BUILD_ALL_TASK_NAME].dependsOn.flatten().containsAll(BUILD_ALL_RELEASE_TASK_NAME, BUILD_ALL_DEBUG_TASK_NAME)
        project.tasks[BUILD_ALL_DEBUG_TASK_NAME].dependsOn.flatten().contains('buildv1')
        project.tasks[BUILD_ALL_RELEASE_TASK_NAME].dependsOn.flatten().contains('buildv2')
    }

    private AndroidVariantConfiguration createVariant(String name, AndroidBuildMode mode) {
        def avc = GroovyMock(AndroidVariantConfiguration)
        avc.name >> name
        avc.mode >> mode
        avc.buildTaskName >> "build$name"
        avc
    }
}
