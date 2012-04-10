package com.apphance.ameba.wp7.plugins.release

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.ProjectHelper;
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration;
import com.apphance.ameba.plugins.release.ProjectReleasePlugin;
import com.apphance.ameba.wp7.Wp7ProjectHelper
import com.apphance.ameba.wp7.plugins.buildplugin.Wp7Plugin;

/**
* Plugin that provides release functionality for Wp7.
*
*  @author Gocal
*
*/
class Wp7ReleasePlugin implements Plugin<Project> {

	static Logger logger = Logging.getLogger(Wp7ReleasePlugin.class)

		Project project
		ProjectHelper projectHelper
		ProjectConfiguration conf
		ProjectReleaseConfiguration releaseConf
		Wp7ProjectHelper wp7projectHelper

	@Override
	public void apply(Project project) {
		ProjectHelper.checkAllPluginsAreLoaded(project, this.class, Wp7Plugin.class, ProjectReleasePlugin.class)
		prepareUpdateVersionTask(project)
	}

	void prepareUpdateVersionTask(Project project) {
		def task = project.task('updateVersion')
		task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
		task.description = """Updates version stored in manifest file of the project.
		   Numeric version is (incremented), String version is set from version.string property"""
		task << {
			use (PropertyCategory) {
				conf.versionString = project.readPropertyOrEnvironmentVariable('version.string')
				wp7projectHelper.updateVersion(project.rootDir, conf)
				logger.lifecycle("New version code: ${conf.versionCode}")
				logger.lifecycle("Updated version string to ${conf.versionString}")
				logger.lifecycle("Configuration : ${conf}")
			}
		}

		//TODO task.dependsOn(project.readAndroidProjectConfiguration)
	}

}
