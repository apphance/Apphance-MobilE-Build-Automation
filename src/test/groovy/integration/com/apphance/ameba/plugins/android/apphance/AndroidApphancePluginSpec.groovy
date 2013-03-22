package com.apphance.ameba.plugins.android.apphance

import com.apphance.ameba.plugins.android.AndroidProjectConfiguration
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
import static com.apphance.ameba.plugins.android.AndroidProjectConfigurationRetriever.ANDROID_PROJECT_CONFIGURATION_KEY
import static com.apphance.ameba.plugins.android.apphance.AndroidApphancePlugin.CONVERT_LOGS_TO_ANDROID_TASK_NAME
import static com.apphance.ameba.plugins.android.apphance.AndroidApphancePlugin.CONVERT_LOGS_TO_APPHANCE_TASK_NAME
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.PREPARE_IMAGE_MONTAGE_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidApphancePluginSpec extends Specification {


    def "plugin tasks' graph configured correctly"() {
        given:
        def project = builder().build()

        and: 'prepare mock configuration'
        def androidConf = Mock(AndroidProjectConfiguration)
        androidConf.buildableVariants >> ['sampleVariant1', 'sampleVariant2', 'sampleVariant3']
        androidConf.debugRelease >> ['sampleVariant1': 'Debug', 'sampleVariant2': 'Release', 'sampleVariant3': 'Debug']

        and: 'add mocked configuration'
        project.ext.set(ANDROID_PROJECT_CONFIGURATION_KEY, androidConf)

        and: 'add faked build tasks to project'
        project.task('buildDebug-sampleVariant1')
        project.task('buildDebug-sampleVariant3')

        and:
        project.plugins.apply(ProjectConfigurationPlugin)

        when:
        project.plugins.apply(AndroidApphancePlugin)

        then: 'apphance configuration was added'
        project.configurations.apphance

        then: 'tasks were added to graph'
        project.tasks['buildDebug-sampleVariant1']
        project.tasks['buildDebug-sampleVariant3']

        then: 'this task is missing'
        !project.tasks.asMap['uploadSamplevariant2']

        then: 'each task has correct group'
        project.tasks[CONVERT_LOGS_TO_APPHANCE_TASK_NAME].group == AMEBA_APPHANCE_SERVICE
        project.tasks[CONVERT_LOGS_TO_ANDROID_TASK_NAME].group == AMEBA_APPHANCE_SERVICE
        project.tasks['uploadSamplevariant1'].group == AMEBA_APPHANCE_SERVICE
        project.tasks['uploadSamplevariant3'].group == AMEBA_APPHANCE_SERVICE

        then: 'each task has correct dependencies'
        project.tasks['uploadSamplevariant1'].dependsOn.containsAll([PREPARE_IMAGE_MONTAGE_TASK_NAME, 'buildDebug-sampleVariant1'])
        project.tasks['uploadSamplevariant3'].dependsOn.containsAll([PREPARE_IMAGE_MONTAGE_TASK_NAME, 'buildDebug-sampleVariant3'])
    }
}
