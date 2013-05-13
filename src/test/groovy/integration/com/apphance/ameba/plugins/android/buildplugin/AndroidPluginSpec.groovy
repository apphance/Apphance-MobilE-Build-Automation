package com.apphance.ameba.plugins.android.buildplugin

import com.apphance.ameba.configuration.android.AndroidBuildMode
import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.ameba.plugins.android.buildplugin.tasks.*
import com.apphance.ameba.plugins.project.tasks.CleanConfTask
import org.gradle.api.plugins.JavaPlugin
import spock.lang.Specification

import static com.apphance.ameba.configuration.android.AndroidBuildMode.DEBUG
import static com.apphance.ameba.configuration.android.AndroidBuildMode.RELEASE
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.plugins.android.buildplugin.AndroidPlugin.*
import static org.gradle.api.plugins.BasePlugin.CLEAN_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidPluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        def ap = new AndroidPlugin()

        and: 'prepare mock configuration'
        def ac = Mock(AndroidConfiguration)
        ac.isEnabled() >> true
        ac.sdkJars >> ['sdk.jar']
        ac.jarLibraries >> ['lib.jar']
        ac.linkedJarLibraries >> ['linkedLib.jar']
        ap.conf = ac

        and:
        def avc = Mock(AndroidVariantsConfiguration)
        avc.variants >> []
        ap.variantsConf = avc

        when:
        ap.apply(project)

        then:
        project.tasks[CleanClassesTask.NAME].group == AMEBA_BUILD
        project.tasks[CopySourcesTask.NAME].group == AMEBA_BUILD
        project.tasks[ReplacePackageTask.NAME].group == AMEBA_BUILD
        project.tasks[UpdateProjectTask.NAME].group == AMEBA_BUILD
        project.tasks[CleanAndroidTask.NAME].group == AMEBA_BUILD
        project.tasks[CompileAndroidTask.NAME].group == AMEBA_BUILD
        project.tasks[BUILD_ALL_TASK_NAME].group == AMEBA_BUILD
        project.tasks[BUILD_ALL_DEBUG_TASK_NAME].group == AMEBA_BUILD
        project.tasks[BUILD_ALL_RELEASE_TASK_NAME].group == AMEBA_BUILD

        and:
        project.tasks[CleanAndroidTask.NAME].dependsOn.flatten().containsAll(CleanConfTask.NAME, UpdateProjectTask.NAME)
        project.tasks[CLEAN_TASK_NAME].dependsOn.flatten().containsAll(CleanAndroidTask.NAME)
        project.tasks[CleanClassesTask.NAME].dependsOn.flatten().containsAll(UpdateProjectTask.NAME)
        project.tasks[CopySourcesTask.NAME].dependsOn.flatten().containsAll(UpdateProjectTask.NAME)
        project.tasks[ReplacePackageTask.NAME].dependsOn.flatten().containsAll(UpdateProjectTask.NAME)
        project.tasks[CompileAndroidTask.NAME].dependsOn.flatten().containsAll(UpdateProjectTask.NAME)
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
        !project.getTasksByName(CleanClassesTask.NAME, false)
        !project.getTasksByName(CopySourcesTask.NAME, false)
        !project.getTasksByName(ReplacePackageTask.NAME, false)
        !project.getTasksByName(UpdateProjectTask.NAME, false)
        !project.getTasksByName(CleanAndroidTask.NAME, false)
        !project.getTasksByName(CompileAndroidTask.NAME, false)
    }

    def 'tasks & variants defined in plugin available when configuration is active'() {
        given:
        def project = builder().build()

        and:
        def ap = new AndroidPlugin()

        and: 'prepare mock configuration'
        def ac = Mock(AndroidConfiguration)
        ac.isEnabled() >> true
        ac.sdkJars >> ['sdk.jar']
        ac.jarLibraries >> ['lib.jar']
        ac.linkedJarLibraries >> ['linkedLib.jar']
        ap.conf = ac

        and:
        def avc = GroovyMock(AndroidVariantsConfiguration)
        avc.variants >> [
                createVariant('v1', DEBUG),
                createVariant('v2', RELEASE)
        ]
        ap.variantsConf = avc

        when:
        ap.apply(project)

        then:
        project.tasks[CleanClassesTask.NAME].group == AMEBA_BUILD
        project.tasks[CopySourcesTask.NAME].group == AMEBA_BUILD
        project.tasks[ReplacePackageTask.NAME].group == AMEBA_BUILD
        project.tasks[UpdateProjectTask.NAME].group == AMEBA_BUILD
        project.tasks[CleanAndroidTask.NAME].group == AMEBA_BUILD
        project.tasks[CompileAndroidTask.NAME].group == AMEBA_BUILD

        and:
        project.tasks['buildv1']
        project.tasks['buildv2']
        project.tasks['installv1']
        project.tasks['installv2']

        and:
        project.tasks['buildv1'].dependsOn.flatten().containsAll(UpdateProjectTask.NAME)
        project.tasks['buildv2'].dependsOn.flatten().containsAll(UpdateProjectTask.NAME)
        project.tasks['installv1'].dependsOn.flatten()*.toString().containsAll('buildv1')
        project.tasks['installv2'].dependsOn.flatten()*.toString().containsAll('buildv2')


        and:
        project.tasks[CleanAndroidTask.NAME].dependsOn.flatten().containsAll(CleanConfTask.NAME)
        project.tasks[CLEAN_TASK_NAME].dependsOn.flatten().containsAll(CleanAndroidTask.NAME)
    }

    private AndroidVariantConfiguration createVariant(String name, AndroidBuildMode mode) {
        def avc = GroovyMock(AndroidVariantConfiguration)
        avc.name >> name
        avc.mode >> mode
        avc
    }
}
