package com.apphance.ameba.vcs.plugins.mercurial;

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.vcs.plugins.VCSPlugin;

/**
 * Plugin for Mercurial implementation of VCS system
 *
 */
class MercurialPlugin extends VCSPlugin {
    static Logger logger = Logging.getLogger(MercurialPlugin.class)


    @Override
    public void apply(Project project) {
        super.apply(project)
        project.task('verifyMercurialSetup', type: VerifyMercurialSetupTask.class)
        project.task('prepareMercurialSetup', type: PrepareMercurialSetupTask.class)
        project.task('showMercurialSetup', type: ShowMercurialSetupTask.class)
    }

    def void cleanVCSTask(Project project) {
        def task = project.task('cleanVCS')
        task.description = "Restores workspace to original state"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_VERSION_CONTROL
        task << {
            String[] commandHgCheck = [
                "rm",
                "-rf",
                ".hgcheck",
            ]
            projectHelper.executeCommand(project, commandHgCheck)
            String[] commandRevert = [
                "hg",
                "revert",
                "-a",
            ]
            projectHelper.executeCommand(project, commandRevert)
            logger.lifecycle("Restored mercurial workspace")
        }
    }

    def void saveReleaseInfoInVCSTask(Project project) {
        def task = project.task('saveReleaseInfoInVCS')
        task.description = "Commits and pushes changes to repository. Requires: hg.commit.user property"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_VERSION_CONTROL
        task << {
            use(PropertyCategory) {
                String commitUser = project.readExpectedProperty('hg.commit.user')
                def commitCommand = [
                    "hg",
                    "commit",
                    "-m",
                    "Incrementing application version to ${conf.versionString} (${conf.versionCode})",
                    "-u",
                    "${commitUser}"
                ]
                if (!conf.commitFilesOnVCS.empty) {
                    conf.commitFilesOnVCS.each {commitCommand << it }
                }
                projectHelper.executeCommand(project, commitCommand as String[])

                String[] tagCommand = [
                    "hg",
                    "tag",
                    "-f",
                    "Release_${conf.versionString}_${conf.versionCode}",
                    "-u",
                    "${commitUser}"
                ]
                projectHelper.executeCommand(project, tagCommand)
                String[] pushCommand = [
                    "hg",
                    "push"
                ]
                projectHelper.executeCommand(project, pushCommand)
                logger.lifecycle("Commited, tagged and pushed ${conf.versionString} (${conf.versionCode})")
            }
        }
    }

    def String [] getVCSExcludes(Project project) {
        return ["**/.hg/**", "**/.hg*/**"]as String[]
    }
}