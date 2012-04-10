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
import com.apphance.ameba.wp7.Wp7ProjectConfiguration;
import com.apphance.ameba.wp7.Wp7ProjectHelper
import com.apphance.ameba.wp7.plugins.buildplugin.Wp7Plugin;
import com.apphance.ameba.plugins.release.ProjectReleaseCategory;
import groovy.text.SimpleTemplateEngine

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
		Wp7ProjectHelper wp7ProjectHelper
		Wp7ProjectConfiguration wp7Conf

	@Override
	public void apply(Project project) {
		ProjectHelper.checkAllPluginsAreLoaded(project, this.class, Wp7Plugin.class, ProjectReleasePlugin.class)
		use (PropertyCategory) {
			this.project = project
			this.projectHelper = new ProjectHelper();
			this.conf = project.getProjectConfiguration()
			this.wp7ProjectHelper = new Wp7ProjectHelper()

			wp7Conf = new Wp7ProjectConfiguration()
			File slnFile = wp7ProjectHelper.getSolutionFile(project.rootDir)
			wp7ProjectHelper.readConfigurationsFromSln(slnFile, wp7Conf)

		}
		use (ProjectReleaseCategory) {
			this.releaseConf = project.getProjectReleaseConfiguration()
		}
		prepareAvailableArtifactsInfoTask(project)
		prepareUpdateVersionTask(project)
		prepareMailMessageTask(project)
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

	private void prepareAvailableArtifactsInfoTask(Project project) {
		def task = project.task('prepareAvailableArtifactsInfo')
	}

	private void prepareMailMessageTask(Project project) {
		def task = project.task('prepareMailMessage')
		task.description = "Prepares mail message which summarises the release"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_RELEASE
		task << {
			releaseConf.mailMessageFile.location.parentFile.mkdirs()
			releaseConf.mailMessageFile.location.delete()
			//logger.lifecycle("Variants: ${androidConf.variants}")
			URL mailTemplate = this.class.getResource("mail_message.html")
			//def mainBuild = "${androidConf.mainVariant}"
			//logger.lifecycle("Main build used for size calculation: ${mainBuild}")
			def fileSize = 0//androidReleaseConf.apkFiles[mainBuild].location.size()
			ResourceBundle rb = ResourceBundle.getBundle(\
				this.class.package.name + ".mail_message",
							releaseConf.locale, this.class.classLoader)
			ProjectReleaseCategory.fillMailSubject(project, rb)
			SimpleTemplateEngine engine = new SimpleTemplateEngine()
			def binding = [
								title : conf.projectName,
								version :conf.fullVersionString,
								currentDate: releaseConf.buildDate,
								otaUrl : "",//androidReleaseConf.otaIndexFile?.url,
								fileIndexUrl: "",//androidReleaseConf.fileIndexFile?.url,
								releaseNotes : releaseConf.releaseNotes,
								fileSize : projectHelper.getHumanReadableSize(fileSize),
								releaseMailFlags : releaseConf.releaseMailFlags,
								rb :rb
							]
			def result = engine.createTemplate(mailTemplate).make(binding)
			releaseConf.mailMessageFile.location.write(result.toString(), "utf-8")
			logger.lifecycle("Mail message file created: ${releaseConf.mailMessageFile}")
		}
		task.dependsOn(project.readProjectConfiguration, project.prepareAvailableArtifactsInfo,
			project.prepareForRelease)
		project.sendMailMessage.dependsOn(task)
	}

}
