package com.apphance.ameba.vcs.plugins.git;

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectHelper;
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.vcs.plugins.AbstractVCSPlugin;


/**
 * Plugin for Git implementation of VCS system
 *
 */
class GitPlugin extends AbstractVCSPlugin {

    static Logger logger = Logging.getLogger(GitPlugin.class)

    ProjectHelper projectHelper = new ProjectHelper()

    @Override
    public void apply(Project project) {
        super.apply(project);
        project.prepareSetup.prepareSetupOperations << new PrepareGitSetupOperation()
        project.verifySetup.verifySetupOperations << new VerifyGitSetupOperation()
        project.showSetup.showSetupOperations << new ShowGitSetupOperation()
    }


    def void cleanVCSTask(Project project) {
        def task = project.task('cleanVCS')
        task.description = "Restores workspace to original state"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_VERSION_CONTROL
        task << {
            String[] gitResetHardCommand = [
                "git",
                "reset",
                "--hard"
            ]
            projectHelper.executeCommand(project, gitResetHardCommand)
            logger.lifecycle("Restored git workspace to original state")
        }
    }

    def void saveReleaseInfoInVCSTask(Project project) {
        def task = project.task('saveReleaseInfoInVCS')
        task.description = "Commits and pushes changes to repository."
        task.group = AmebaCommonBuildTaskGroups.AMEBA_VERSION_CONTROL
        task << {
            def gitBranch
            use (PropertyCategory) {
                gitBranch = project.readProperty("git.branch")
            }
            if (gitBranch == null) {
                gitBranch = "master"
            }
            String[] checkoutCommand = [
                "git",
                "checkout",
                "${gitBranch}"
            ]
            projectHelper.executeCommand(project, checkoutCommand)
            String[] stageCommand = [
                "git",
                "stage",
                "."
            ]
            projectHelper.executeCommand(project, stageCommand)
            def commitCommand = [
                "git",
                "commit",
            ]
            if (conf.commitFilesOnVCS.empty) {
                conf.commitFilesOnVCS.each {commitCommand << "-a" }
            }
            commitCommand << "-m"
            commitCommand <<  "Incrementing application version to ${conf.versionString} (${conf.versionCode})"
            if (!conf.commitFilesOnVCS.empty) {
                conf.commitFilesOnVCS.each {commitCommand << it }
            }
            projectHelper.executeCommand(project, commitCommand as String[])
            String[] revParseCommand = [
                "git",
                "rev-parse",
                "HEAD"
            ]
            def revision = projectHelper.executeCommand(project, revParseCommand)[0]
            String[] tagCommand = [
                "git",
                "tag",
                "-a",
                "-m",
                "Tagging Revision ${conf.versionString}_${conf.versionCode}",
                "Release_${conf.versionString}_${conf.versionCode}",
                revision
            ]
            projectHelper.executeCommand(project, tagCommand)
            String[] pullCommand = [
                "git",
                "pull",
                "origin"
            ]
            projectHelper.executeCommand(project, pullCommand)
            String[] pushTagsCommand = [
                "git",
                "push",
                "origin",
                "--tags"
            ]
            projectHelper.executeCommand(project, tagCommand)
            projectHelper.executeCommand(project, pullCommand)
            String[] pushAllCommand = [
                "git",
                "push",
                "origin",
                "--all"
            ]
            projectHelper.executeCommand(project, pushAllCommand)
            logger.lifecycle("Commited, tagged and pushed ${conf.versionString} (${conf.versionCode})")
        }
    }

    def String [] getVCSExcludes(Project project) {
        return ["**/.git/*"]as String[]
    }

    void prepareShowPropertiesTask(Project arg0) {
        // no properties to show
    }

    static public final String DESCRIPTION =
"""This is the VCS (version control system) plugin which supports git VCS.

The plugin should be applied before the main build plugin.
"""

}
