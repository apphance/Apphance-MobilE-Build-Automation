package com.apphance.ameba.android.plugins.release

import com.apphance.ameba.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.android.AndroidSingleVariantJarBuilder
import com.apphance.ameba.android.plugins.release.tasks.AvailableArtifactsInfoTask
import com.apphance.ameba.android.plugins.release.tasks.BuildDocZipTask
import com.apphance.ameba.android.plugins.release.tasks.MailMessageTask
import com.apphance.ameba.android.plugins.release.tasks.UpdateVersionTask
import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import javax.inject.Inject

import static com.apphance.ameba.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.getREAD_PROJECT_CONFIGURATION_TASK_NAME
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.PREPARE_FOR_RELEASE_TASK_NAME
import static com.apphance.ameba.plugins.release.ProjectReleasePlugin.SEND_MAIL_MESSAGE_TASK_NAME
import static org.gradle.api.logging.Logging.getLogger
import static org.gradle.api.plugins.JavaPlugin.JAVADOC_TASK_NAME

/**
 * Plugin that provides release functionality for android.
 *
 */
class AndroidReleasePlugin implements Plugin<Project> {

    public static final String BUILD_DOCUMENTATION_ZIP_TASK_NAME = 'buildDocumentationZip'
    public static final String PREPARE_AVAILABLE_ARTIFACTS_INFO_TASK_NAME = 'prepareAvailableArtifactsInfo'
    public static final String PREPARE_MAIL_MESSAGE_TASK_NAME = 'prepareMailMessage'
    public static final String UPDATE_VERSION_TASK_NAME = 'updateVersion'

    def l = getLogger(AndroidReleasePlugin.class)

    @Inject
    private CommandExecutor executor

    private Project project

    @Override
    public void apply(Project project) {
        this.project = project

        prepareUpdateVersionTask()
        prepareBuildDocumentationZipTask()
        prepareAvailableArtifactsInfoTask()
        prepareMailMessageTask()

        //TODO to be separated, refactored, redesigned :/
        AndroidSingleVariantApkBuilder.buildListeners << new AndroidReleaseApkListener(project, executor)
        AndroidSingleVariantJarBuilder.buildListeners << new AndroidReleaseJarListener(project, executor)
    }

    private void prepareBuildDocumentationZipTask() {
        Task task = project.task(BUILD_DOCUMENTATION_ZIP_TASK_NAME)
        task.description = 'Builds documentation .zip file'
        task.group = AMEBA_RELEASE
        task.doLast { new BuildDocZipTask(project).buildDocZip() }
        task.dependsOn(JAVADOC_TASK_NAME,
                READ_PROJECT_CONFIGURATION_TASK_NAME,
                PREPARE_FOR_RELEASE_TASK_NAME)
    }

    private void prepareAvailableArtifactsInfoTask() {
        Task task = project.task(PREPARE_AVAILABLE_ARTIFACTS_INFO_TASK_NAME)
        task.description = 'Prepares information about available artifacts for mail message to include'
        task.group = AMEBA_RELEASE
        task.doLast { new AvailableArtifactsInfoTask(project, executor).availableArtifactsInfo() }
        task.dependsOn('readAndroidProjectConfiguration')
    }

    private void prepareMailMessageTask() {
        def task = project.task(PREPARE_MAIL_MESSAGE_TASK_NAME)
        task.description = 'Prepares mail message which summarises the release'
        task.group = AMEBA_RELEASE
        task.doLast { new MailMessageTask(project).mailMessage() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME,
                PREPARE_AVAILABLE_ARTIFACTS_INFO_TASK_NAME,
                PREPARE_FOR_RELEASE_TASK_NAME)
        project.tasks.getByName(SEND_MAIL_MESSAGE_TASK_NAME).dependsOn(PREPARE_MAIL_MESSAGE_TASK_NAME)
    }

    private void prepareUpdateVersionTask() {
        Task task = project.task(UPDATE_VERSION_TASK_NAME)
        task.group = AMEBA_RELEASE
        task.description = """Updates version stored in manifest file of the project.
           Numeric version is set from 'version.code' property, String version is set from 'version.string' property"""
        task.doLast { new UpdateVersionTask(project).updateVersion() }
        task.dependsOn('readAndroidProjectConfiguration')
    }

    static public final String DESCRIPTION =
        """This is the plugin that provides simple release functionality.

It provides basic release tasks, so that you can upgrade version of the application
while preparing the release and it provides post-release tasks that commit it into the repository.
Most importantly however, it produces ready-to-use OTA (Over-The-Air) package (in ota directory)
that you can copy to appropriate directory on your web server and have ready-to-use,
easily installable OTA version of your application.

Note that you need to load generic 'ameba-project-release' plugin before this plugin is loaded.
"""
}
