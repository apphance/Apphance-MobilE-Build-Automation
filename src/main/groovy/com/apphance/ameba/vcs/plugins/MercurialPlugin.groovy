package com.apphance.ameba.vcs.plugins;

import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups

/**
 * Plugin for Mercurial implementation of VCS system
 *
 */
class MercurialPlugin extends VCSPlugin {

	enum MercurialProperty {
		
		COMMIT_USER(false, "hg.commit.user", "Mail of commit user")
		
		private final boolean optional
		private final String name
		private final String description
	
		MercurialProperty(boolean optional, String name, String description) {
			this.optional = optional
			this.name = name
			this.description = description
		}
	
		public boolean isOptional() {
			return optional
		}
	
		public String getName() {
			return name
		}
		
		public String getDescription() {
			return description
		}
	}
	
    static Logger logger = Logging.getLogger(MercurialPlugin.class)
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
            String commitUser = projectHelper.getExpectedProperty(project, 'hg.commit.user')
            def commitCommand = [
                "hg",
                "commit",
                "-m",
                "Incrementing application version to ${conf.versionString} (${conf.versionCode}",
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

    def String [] getVCSExcludes(Project project) {
        return ["**/.hg/**", "**/.hg*/**"]as String[]
    }

	@Override
	public void prepareShowPropertiesTask(Project project) {
		def task = project.task('showMercurialProperties')
		task.description = "Prints Mercurial properties"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
		task << {
			System.out.println("""###########################################################
# Mercurial properties
###########################################################""")
			for (MercurialProperty property : MercurialProperty.values()) {
				String comment = '# ' + property.getDescription()
				String propString = property.getName() + '='
				if (property.isOptional()) {
					comment = comment + ' [optional]'
				} else {
					comment = comment + ' [required]'
				}
				if (project.hasProperty(property.getName())) {
					propString = propString +  project[property.getName()]
				}

				if (project.showProperties.showComments == true) {
					System.out.println(comment)
				}
				System.out.println(propString)
			}
		}
		project.showProperties.dependsOn(task)
	}
}