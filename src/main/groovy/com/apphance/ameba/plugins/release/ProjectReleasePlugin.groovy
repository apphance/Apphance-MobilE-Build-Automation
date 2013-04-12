package com.apphance.ameba.plugins.release

import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.release.tasks.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logging

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin.READ_PROJECT_CONFIGURATION_TASK_NAME
import static org.gradle.api.plugins.BasePlugin.CLEAN_TASK_NAME

/**
 * Plugin for releasing projects.
 *
 */
class ProjectReleasePlugin implements Plugin<Project> {

    def l = Logging.getLogger(getClass())

    public final static String COPY_GALLERY_FILES_TASK_NAME = 'copyGalleryFiles'
    public final static String PREPARE_FOR_RELEASE_TASK_NAME = 'prepareForRelease'
    public final static String VERIFY_RELEASE_NOTES_TASK_NAME = 'verifyReleaseNotes'
    public final static String PREPARE_IMAGE_MONTAGE_TASK_NAME = 'prepareImageMontage'
    public final static String SEND_MAIL_MESSAGE_TASK_NAME = 'sendMailMessage'
    public final static String PREPARE_MAIL_MESSAGE_TASK_NAME = 'prepareMailMessage'
    public final static String CLEAN_RELEASE_TASK_NAME = 'cleanRelease'
    public final static String BUILD_SOURCES_ZIP_TASK_NAME = 'buildSourcesZip'

    @Inject
    CommandExecutor executor

    private Project project

    @Override
    void apply(Project project) {
        this.project = project

        //must be here to fill the ProjectReleaseConfiguration object
        //to be removed when auto-detection done
        ProjectReleaseCategory.retrieveProjectReleaseData(project)

        prepareMailConfiguration()
        prepareCopyGalleryFilesTask()
        preparePrepareForReleaseTask()
        prepareVerifyReleaseNotesTask()
        prepareImageMontageTask()
        prepareSendMailMessageTask()
        prepareCleanReleaseTask()
        prepareSourcesZipTask()

        project.prepareSetup.prepareSetupOperations << new PrepareReleaseSetupOperation()
        project.verifySetup.verifySetupOperations << new VerifyReleaseSetupOperation()
        project.showSetup.showSetupOperations << new ShowReleaseSetupOperation()
    }

    private void prepareMailConfiguration() {
        project.configurations.add('mail')
        project.dependencies {
            mail 'org.apache.ant:ant-javamail:1.9.0'
            mail 'javax.mail:mail:1.4'
            mail 'javax.activation:activation:1.1.1'
        }
    }

    private void prepareCopyGalleryFilesTask() {
        Task task = project.task(COPY_GALLERY_FILES_TASK_NAME)
        task.group = AMEBA_CONFIGURATION
        task.description = 'Copy files required by swipe jquerymobile gallery'
        task.doLast { new CopyGalleryFilesTask(project).copy() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME)
    }

    private void preparePrepareForReleaseTask() {
        def task = project.task(PREPARE_FOR_RELEASE_TASK_NAME)
        task.group = AMEBA_RELEASE
        task.description = 'Prepares project for release'
        task.doLast { new PrepareForReleaseTask(project).prepare() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME, COPY_GALLERY_FILES_TASK_NAME)
    }

    private void prepareVerifyReleaseNotesTask() {
        Task task = project.task(VERIFY_RELEASE_NOTES_TASK_NAME)
        task.group = AMEBA_RELEASE
        task.description = 'Verifies that release notes are set for the build'
        task.doLast { new VerifyReleaseNotesTask(project).verify() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME, PREPARE_FOR_RELEASE_TASK_NAME)
    }

    private void prepareImageMontageTask() {
        def task = project.task(PREPARE_IMAGE_MONTAGE_TASK_NAME)
        task.description = 'Builds montage of images found in the project'
        task.group = AMEBA_RELEASE
        task.doLast { new ImageMontageTask(project, executor).imageMontage() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME, PREPARE_FOR_RELEASE_TASK_NAME)
    }

    private void prepareSendMailMessageTask() {
        Task task = project.task(SEND_MAIL_MESSAGE_TASK_NAME)
        task.description = """Sends mail message. Requires mail.server, mail.port properties
             or corresponding MAIL_SERVER, MAIL_PORT env variables (no authentication).
             It also uses certain properties to send mails:
             release.mail.from, release.mail.to, release.mail.flags
             flags are one of: qrCode,imageMontage"""
        task.group = AMEBA_RELEASE
        task.doLast { new SendMailMessageTask(project).sendMailMessage() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME, PREPARE_FOR_RELEASE_TASK_NAME, VERIFY_RELEASE_NOTES_TASK_NAME, PREPARE_MAIL_MESSAGE_TASK_NAME)
    }

    private void prepareCleanReleaseTask() {
        Task task = project.task(CLEAN_RELEASE_TASK_NAME)
        task.description = 'Cleans release related directories'
        task.group = AMEBA_RELEASE
        task.doLast { new CleanReleaseTask(project).clean() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME, CLEAN_TASK_NAME)
    }

    private void prepareSourcesZipTask() {
        Task task = project.task(BUILD_SOURCES_ZIP_TASK_NAME)
        task.description = 'Builds sources .zip file.'
        task.group = AMEBA_RELEASE
        task.doLast { new BuildSourcesZipTask(project).buildSourcesZip() }
        task.dependsOn(READ_PROJECT_CONFIGURATION_TASK_NAME, PREPARE_FOR_RELEASE_TASK_NAME)
    }

    static public final String DESCRIPTION =
        """This is Ameba release plugin.

The plugin provides all the basic tasks required to prepare OTA release of
an application. It should be added after build plugin is added.
"""
}