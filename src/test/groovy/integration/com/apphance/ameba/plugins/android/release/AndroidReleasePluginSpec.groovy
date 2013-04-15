package com.apphance.ameba.plugins.android.release

import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.plugins.android.release.tasks.AvailableArtifactsInfoTask
import com.apphance.ameba.plugins.android.release.tasks.BuildDocZipTask
import com.apphance.ameba.plugins.android.release.tasks.MailMessageTask
import com.apphance.ameba.plugins.android.release.tasks.UpdateVersionTask
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.plugins.android.buildplugin.AndroidPlugin.READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.PREPARE_FOR_RELEASE_TASK_NAME
import static org.gradle.api.plugins.JavaPlugin.JAVADOC_TASK_NAME
import static org.gradle.testfixtures.ProjectBuilder.builder

class AndroidReleasePluginSpec extends Specification {

    def 'tasks defined in plugin available when configuration is active'() {

        given:
        def project = builder().build()

        and:
        def arp = new AndroidReleasePlugin()

        and: 'create mock android release configuration and set it'
        def arc = Spy(AndroidReleaseConfiguration)
        arc.isActive() >> true
        arp.releaseConf = arc

        when:
        arp.apply(project)

        then: 'every single task is in correct group'
        project.tasks[UpdateVersionTask.NAME].group == AMEBA_RELEASE
        project.tasks[BuildDocZipTask.NAME].group == AMEBA_RELEASE
        project.tasks[AvailableArtifactsInfoTask.NAME].group == AMEBA_RELEASE
        project.tasks[MailMessageTask.NAME].group == AMEBA_RELEASE

        then: 'every task has correct dependencies'
        project.tasks[UpdateVersionTask.NAME].dependsOn.contains(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)

        project.tasks[BuildDocZipTask.NAME].dependsOn.flatten().containsAll(JAVADOC_TASK_NAME,
                READ_PROJECT_CONFIGURATION_TASK_NAME,
                PREPARE_FOR_RELEASE_TASK_NAME)

        project.tasks[AvailableArtifactsInfoTask.NAME].dependsOn.contains(READ_ANDROID_PROJECT_CONFIGURATION_TASK_NAME)

        project.tasks[MailMessageTask.NAME].dependsOn.flatten().containsAll(READ_PROJECT_CONFIGURATION_TASK_NAME,
                AvailableArtifactsInfoTask.NAME,
                PREPARE_FOR_RELEASE_TASK_NAME)
    }

    def 'no tasks available when configuration is inactive'() {
        given:
        def project = builder().build()

        and:
        def arp = new AndroidReleasePlugin()

        and: 'create mock android release configuration and set it'
        def arc = Spy(AndroidReleaseConfiguration)
        arc.isActive() >> false
        arp.releaseConf = arc

        when:
        arp.apply(project)

        then:
        !project.getTasksByName(UpdateVersionTask.NAME, false)
        !project.getTasksByName(BuildDocZipTask.NAME, false)
        !project.getTasksByName(AvailableArtifactsInfoTask.NAME, false)
        !project.getTasksByName(MailMessageTask.NAME, false)
    }
}
