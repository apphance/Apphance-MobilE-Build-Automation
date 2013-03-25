package com.apphance.ameba.plugins.ios.release

import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.ios.release.tasks.BuildDocZipTask
import com.apphance.ameba.plugins.ios.release.tasks.PrepareAvailableArtifactsInfoTask
import com.apphance.ameba.plugins.ios.release.tasks.PrepareMailMessageTask
import com.apphance.ameba.plugins.ios.release.tasks.UpdateVersionTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.plugins.ios.buildplugin.IOSPlugin.READ_IOS_PROJECT_VERSIONS_TASK_NAME
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.SEND_MAIL_MESSAGE_TASK_NAME
import static org.gradle.api.logging.Logging.getLogger

/**
 * Plugin for releasing iOS build.
 *
 */
class IOSReleasePlugin implements Plugin<Project> {

    def l = getLogger(getClass())

    public static final String UPDATE_VERSION_TASK_NAME = 'updateVersion'
    public static final String BUILD_DOCUMENTATION_ZIP_TASK_NAME = 'buildDocumentationZip'
    public static final String PREPARE_AVAILABLE_ARTIFACTS_INFO_TASK_NAME = 'prepareAvailableArtifactsInfo'
    public static final String PREPARE_MAIL_MESSAGE_TASK_NAME = 'prepareMailMessage'

    @Inject
    private CommandExecutor executor
    @Inject
    private IOSExecutor iosExecutor
    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        prepareUpdateVersionTask()
        prepareBuildDocumentationZipTask()
        prepareAvailableArtifactsInfoTask()
        prepareMailMessageTask()
    }

    private void prepareUpdateVersionTask() {
        def task = project.task(UPDATE_VERSION_TASK_NAME)
        task.group = AMEBA_RELEASE
        task.description = """Updates version stored in plist file of the project.
           Numeric version is set from 'version.code' property, String version is set from 'version.string' property"""
        task << { new UpdateVersionTask(project).updateVersion() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)
    }

    private void prepareBuildDocumentationZipTask() {
        def task = project.task(BUILD_DOCUMENTATION_ZIP_TASK_NAME)
        task.description = 'Builds documentation .zip file.'
        task.group = AMEBA_RELEASE
        task << { new BuildDocZipTask().buildDocZip() }
    }

    private void prepareAvailableArtifactsInfoTask() {
        def task = project.task(PREPARE_AVAILABLE_ARTIFACTS_INFO_TASK_NAME)
        task.description = 'Prepares information about available artifacts for mail message to include'
        task.group = AMEBA_RELEASE
        task << { new PrepareAvailableArtifactsInfoTask(project, executor, iosExecutor).prepareAvailableArtifactsInfo() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)
        task.dependsOn(READ_IOS_PROJECT_VERSIONS_TASK_NAME)
    }

    private void prepareMailMessageTask() {
        def task = project.task(PREPARE_MAIL_MESSAGE_TASK_NAME)
        task.description = 'Prepares mail message which summarises the release'
        task.group = AMEBA_RELEASE
        task << { new PrepareMailMessageTask(project).prepareMailMessage() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME, PREPARE_AVAILABLE_ARTIFACTS_INFO_TASK_NAME)
        def sendMailTask = project.tasks.getByName(SEND_MAIL_MESSAGE_TASK_NAME)
        sendMailTask.dependsOn(task)
        sendMailTask.description += ',installableSimulator'
    }

    static public final String DESCRIPTION =
        """This is the plugin that provides simple release functionality.

It provides basic release tasks, so that you can upgrade version of the application
while preparing the release and it provides post-release tasks that commit it into the repository.
Most importantly however, it produces ready-to-use OTA (Over-The-Air) package (in ota directory)
that you can copy to appropriate directory on your web server and have ready-to-use,
easily installable OTA version of your application.

Note that you need to load generic 'ameba-project-release' plugin before this plugin is loaded."""
}
