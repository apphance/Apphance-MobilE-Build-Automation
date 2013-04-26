package com.apphance.ameba.plugins.ios.buildplugin

import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups
import com.apphance.ameba.plugins.ios.buildplugin.tasks.*
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.*

/**
 * Plugin for various X-Code related tasks.
 *
 */
//TODO write a test for this plugin to check task grap after configuration, auto - detection done
class IOSPlugin implements Plugin<Project> {

    static final String IOS_CONFIGURATION_LOCAL_PROPERTY = 'ios.configuration'

    public static final String COPY_MOBILE_PROVISION_TASK_NAME = 'copyMobileProvision'
    public static final String READ_IOS_PROJECT_VERSIONS_TASK_NAME = 'readIOSProjectVersions'
    public static final String IOS_TARGET_LOCAL_PROPERTY = 'ios.target'
    public static final String BUILD_ALL_SIMULATORS_TASK_NAME = 'buildAllSimulators'
    public static final String COPY_SOURCES_TASK_NAME = 'copySources'
    public static final String COPY_DEBUG_SOURCES_TASK_NAME = 'copyDebugSources'
    public static final String READ_IOS_PROJECT_CONFIGURATION_TASK_NAME = 'readIOSProjectConfiguration'
    public static final String CLEAN_TASK_NAME = 'clean'
    public static final String UNLOCK_KEY_CHAIN_TASK_NAME = 'unlockKeyChain'
    public static final String BUILD_ALL_TASK_NAME = 'prepareAllTasks'
    public static final String BUILD_SINGLE_VARIANT_TASK_NAME = 'buildSingleVariant'

    public static final List<String> FAMILIES = ['iPad', 'iPhone']

    @Inject
    private CommandExecutor executor
    @Inject
    private IOSExecutor iosExecutor

    private Project project
    private ProjectConfiguration conf

    @Override
    def void apply(Project project) {
        this.project = project
        this.conf = getProjectConfiguration(project)

        //TODO to be removed, after audodetection - configuration implemented :/
        new ReadIOSProjectConfigurationTask(project, iosExecutor).readIosProjectConfiguration()

        prepareCopySourcesTask()
        prepareCopyDebugSourcesTask()
        prepareReadIosProjectConfigurationTask()
        prepareReadIosProjectVersionsTask()
        prepareCleanTask()
        prepareUnlockKeyChainTask()
        prepareCopyMobileProvisionTask()
        prepareBuildSingleVariantTask()
        prepareBuildAllSimulatorsTask()
        prepareBuildAllTask()

        addIosSourceExcludes()

        project.prepareSetup.prepareSetupOperations << new PrepareIOSSetupOperation()
        project.verifySetup.verifySetupOperations << new VerifyIOSSetupOperation()
        project.showSetup.showSetupOperations << new ShowIOSSetupOperation()
    }

    private void prepareCopySourcesTask() {
        def task = project.task(COPY_SOURCES_TASK_NAME)
        task.description = 'Copies all sources to tmp directories for build'
        task.group = AMEBA_BUILD
        task << { new CopySourcesTask(project).copySources() }
    }

    private void prepareCopyDebugSourcesTask() {
        def task = project.task(COPY_DEBUG_SOURCES_TASK_NAME)
        task.description = 'Copies all debug sources to tmp directories for build'
        task.group = AMEBA_BUILD
        task << { new CopyDebugSourcesTask(project).copyDebugSources() }
    }

    private void prepareReadIosProjectConfigurationTask() {
        def task = project.task(READ_IOS_PROJECT_CONFIGURATION_TASK_NAME)
        task.group = AMEBA_CONFIGURATION
        task.description = 'Reads iOS project configuration'
        task << { new ReadIOSProjectConfigurationTask(project, iosExecutor).readIosProjectConfiguration() }
        project.tasks[READ_PROJECT_CONFIGURATION_TASK_NAME].dependsOn(task)
    }

    private void prepareReadIosProjectVersionsTask() {
        def task = project.task(READ_IOS_PROJECT_VERSIONS_TASK_NAME)
        task.group = AMEBA_CONFIGURATION
        task.description = 'Reads iOS project version information'
        task << { new ReadIOSProjectVersionsTask(project).readIOSProjectVersions() }
        project.tasks[READ_PROJECT_CONFIGURATION_TASK_NAME].dependsOn(task)
    }

    private void prepareCleanTask() {
        def task = project.task(CLEAN_TASK_NAME)
        task.description = 'Cleans the project'
        task.group = AMEBA_BUILD
        task << { new CleanTask(project, executor).clean() }
        task.dependsOn(CLEAN_CONFIGURATION_TASK_NAME)
    }

    private void prepareUnlockKeyChainTask() {
        def task = project.task(UNLOCK_KEY_CHAIN_TASK_NAME)
        task.description = """Unlocks key chain used during project building.
              Requires osx.keychain.password and osx.keychain.location properties
              or OSX_KEYCHAIN_PASSWORD and OSX_KEYCHAIN_LOCATION environment variable"""
        task.group = AMEBA_BUILD
        task << { new UnlockKeyChainTask(project, executor).unlockKeyChain() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)
    }

    private void prepareCopyMobileProvisionTask() {
        def task = project.task(COPY_MOBILE_PROVISION_TASK_NAME)
        task.description = 'Copies mobile provision file to the user library'
        task.group = AMEBA_BUILD
        task << { new CopyMobileProvisionTask(project).copyMobileProvision() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)
    }

    private void prepareBuildSingleVariantTask() {
        def task = project.task(BUILD_SINGLE_VARIANT_TASK_NAME)
        task.group = AMEBA_BUILD
        task.description = 'Builds single variant for iOS. Requires ios.target and ios.configuration properties'
        task << { new BuildSingleVariantTask(project, iosExecutor).buildSingleVariant() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME, VERIFY_SETUP_TASK_NAME, COPY_SOURCES_TASK_NAME)
    }

    private void prepareBuildAllSimulatorsTask() {
        def task = project.task(BUILD_ALL_SIMULATORS_TASK_NAME)
        task.group = AMEBA_BUILD
        task.description = 'Builds all simulators for the project'
        task << { new IOSAllSimulatorsBuilder(project, executor, iosExecutor).buildAllSimulators() }
        task.dependsOn(
                READ_PROJECT_CONFIGURATION_TASK_NAME,
                COPY_MOBILE_PROVISION_TASK_NAME,
                COPY_DEBUG_SOURCES_TASK_NAME)
    }

    private void prepareBuildAllTask() {
        def task = project.task(BUILD_ALL_TASK_NAME)
        task.group = AMEBA_BUILD
        task.description = 'Builds all target/configuration combinations and produces all artifacts (zip, ipa, messages, etc)'
        List<String> dependsOn = new BuildAllTask(project, executor, iosExecutor).prepareAllTasks()
        task.dependsOn(dependsOn)
    }

    private addIosSourceExcludes() {
        conf.sourceExcludes << '**/build/**'
    }

    static public final String DESCRIPTION =
        """This is the main iOS build plugin.

The plugin provides all the task needed to build iOS application.
Besides tasks explained below, the plugin prepares build-*
tasks which are dynamically created, based on targets and configurations available.
There is one task available per each Target-Configuration combination - unless particular
combination is excluded by the exclude property."""
}
