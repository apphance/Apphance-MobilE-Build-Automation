package com.apphance.ameba.plugins.ios

import com.apphance.ameba.plugins.AmebaPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.*
import static com.apphance.ameba.plugins.ios.buildplugin.IOSProjectProperty.DISTRIBUTION_DIR
import static com.apphance.ameba.plugins.ios.buildplugin.IOSProjectProperty.PLIST_FILE

class IOSTaskSpec extends Specification {

    @Shared
    def project

    @Shared
    def buildTasks = [
            'clean',
            'prepareAllTasks',
            'buildAllSimulators',
            'build-GradleXCode-BasicConfiguration',
            'buildSingleVariant',
            'copyMobileProvision',
            'unlockKeyChain',
            'copySources',
            'copyDebugSources',
    ]

    @Shared
    def confTasks = [
            'cleanConfiguration',
            'readProjectConfiguration',
            'readIOSProjectConfiguration',
            'readIOSProjectVersions',
    ]

    @Shared
    def setupTasks = [
            'prepareSetup',
            'verifySetup',
            'showSetup',
            'showConventions',
    ]

    @Shared
    def releaseTasks = [
            'cleanRelease',
            'updateVersion',
            'buildDocumentationZip',
            'buildSourcesZip',
            'prepareAvailableArtifactsInfo',
            'prepareForRelease',
            'prepareImageMontage',
            'prepareMailMessage',
            'sendMailMessage',
            'verifyReleaseNotes',
    ]

    def setupSpec() {
        def projectBuilder = ProjectBuilder.builder()
        projectBuilder.withProjectDir(new File('testProjects/ios/GradleXCode'))
        project = projectBuilder.build()
        project.ext[PLIST_FILE.propertyName] = 'Test.plist'
        project.ext[DISTRIBUTION_DIR.propertyName] = 'release/distribution_resources'
        project.project.plugins.apply(AmebaPlugin.class)
    }

    def 'plugins task are in correct group'() {

        when:
        def groups = tasks.collect { project.tasks[it].group }

        then:
        tasksGroups == groups

        where:
        tasks        | tasksGroups
        buildTasks   | [AMEBA_BUILD] * buildTasks.size()
        confTasks    | [AMEBA_CONFIGURATION] * confTasks.size()
        setupTasks   | [AMEBA_SETUP] * setupTasks.size()
        releaseTasks | [AMEBA_RELEASE] * releaseTasks.size()
    }

    def 'tasks contains correct operations'() {

        expect:
        tasksClasses.containsAll(classes)

        where:
        classes                                                      | tasksClasses
        ['PrepareIOSSetupOperation', 'PrepareReleaseSetupOperation'] | project.prepareSetup.prepareSetupOperations*.class.simpleName
        ['VerifyIOSSetupOperation', 'VerifyReleaseSetupOperation']   | project.verifySetup.verifySetupOperations*.class.simpleName
        ['ShowIOSSetupOperation', 'ShowReleaseSetupOperation']       | project.showSetup.showSetupOperations*.class.simpleName
    }
}
