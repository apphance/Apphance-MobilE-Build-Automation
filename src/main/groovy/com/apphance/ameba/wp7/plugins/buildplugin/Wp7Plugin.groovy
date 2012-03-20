package com.apphance.ameba.wp7.plugins.buildplugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.wp7.Wp7ProjectConfiguration
import com.apphance.ameba.wp7.Wp7ProjectHelper
import com.apphance.ameba.wp7.Wp7SingleVariantBuilder


/**
 * Plugin for various Windows Phone 7 related tasks.
 *
 */
class Wp7Plugin implements Plugin<Project> {

	static Logger logger = Logging.getLogger(Wp7Plugin.class)

	ProjectHelper projectHelper
	Wp7ProjectHelper wp7ProjectHelper
	ProjectConfiguration conf
	Wp7ProjectConfiguration wp7Conf;

	public void apply(Project project) {

		use (PropertyCategory) {
			this.projectHelper = new ProjectHelper()
			wp7ProjectHelper = new Wp7ProjectHelper()
			this.conf = project.getProjectConfiguration()
			wp7Conf = new Wp7ProjectConfiguration()

			prepareCopySourcesTask(project)
			prepareBuildAllTask(project)
			prepareCleanTask(project)

			project.prepareSetup.prepareSetupOperations << new PrepareWp7SetupOperation()
			project.verifySetup.verifySetupOperations << new VerifyWp7SetupOperation()
			project.showSetup.showSetupOperations << new ShowWp7PropertiesOperation()
		}
	}


	private prepareReadWp7ProjectConfigurationTask(Project project) {
		def task = project.task('readWp7ProjectConfiguration')
		task.group = AmebaCommonBuildTaskGroups.AMEBA_CONFIGURATION
		task.description = 'Reads Wp7 project configuration'
		task << {
			use (PropertyCategory) {
				File slnFile = wp7ProjectHelper.getSolutionFile(project.rootDir)
				wp7ProjectHelper.readConfigurationsFromSln(slnFile, wp7Conf)
			}
		}

		project.readProjectConfiguration.dependsOn(task)
	}

	void prepareBuildAllTask(Project project) {
		def task = project.task('buildAll')
		task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
		task.description = 'Builds all target/configuration combinations and produces all artifacts'
		File slnFile = wp7ProjectHelper.getSolutionFile(project.rootDir)
		wp7ProjectHelper.readConfigurationsFromSln(slnFile, wp7Conf)

		def targets = wp7Conf.targets
		def configurations = wp7Conf.configurations

		targets.each { target ->
			configurations.each { configuration ->
				def id = "${target}${configuration}".toString()
				def noSpaceId = id.replaceAll(' ','_')
				def singleTask = project.task("build${noSpaceId}")
				singleTask.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
				singleTask.description = "Builds target:${target} configuration:${configuration}"
				singleTask << {
					def singleReleaseBuilder = new Wp7SingleVariantBuilder(wp7Conf)
					singleReleaseBuilder.buildRelease(project, target, configuration)
				}
				task.dependsOn(singleTask)
				singleTask.dependsOn(project.readProjectConfiguration, project.verifySetup, project.copySources)
			}
		}

		task.dependsOn(project.readProjectConfiguration)
	}

	private void prepareCleanTask(Project project) {
		def task = project.task('clean')
		task.description = "Cleans the project"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
		task << {
			projectHelper.executeCommand(project, ['MSBuild', "/target:Clean"])
		}
		task.dependsOn(project.cleanConfiguration)
	}


	void prepareCopySourcesTask(Project project) {
		def task = project.task('copySources')
		task.description = "Copies all sources to tmp directory for build"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
		task << {

			def targets = wp7Conf.targets
			def configurations = wp7Conf.configurations

			targets.each { target ->
				configurations.each { configuration ->

					File projectDir = project.rootDir
					File variantDir = wp7Conf.getVariantDirectory(project, target, configuration)

					new AntBuilder().sync(toDir : variantDir, overwrite:true, verbose:true) {
						fileset(dir : projectDir) {
//							exclude(name: new File(project.rootDir, iosConf.tmpDirName(target, configuration)).absolutePath + '/**/*')
//							conf.sourceExcludes.each { exclude(name: it) }
						}
					}
				}
			}
		}
	}


	static public final String DESCRIPTION =
	"""This is the main Windows Phone build plugin.

	The plugin provides all the tasks needed to build windows phone application.
	Besides tasks explained below, the plugin prepares build-* and install-*
	tasks which are dynamically created, based on variants available. In
	case the build has no variants, the only available builds are Debug and Release.
	In case of variants, there is one build and one task created for every variant.
	"""
}
