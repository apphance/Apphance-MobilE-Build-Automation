package com.apphance.ameba.wp7.plugins.test

import java.io.File

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.wp7.plugins.buildplugin.Wp7ProjectProperty



/**
 * Plugin for various Windows Phone 7 related tasks.
 *
 */
class Wp7TestPlugin implements Plugin<Project> {

	static Logger logger = Logging.getLogger(Wp7TestPlugin.class)

	ProjectHelper projectHelper
	ProjectConfiguration conf
	File projectTestDirectory


	public void apply(Project project) {
		use (PropertyCategory) {
			this.projectHelper = new ProjectHelper()
			this.conf = project.getProjectConfiguration()


			String testDirectory = project.readProperty(Wp7ProjectProperty.TEST_DIRECTORY)

			if (testDirectory != null) {
				projectTestDirectory = new File(project.rootDir, testDirectory)
			} else {
				projectTestDirectory = new File(project.rootDir,"test/"+project.name+".Tests")
			}

			prepareWp7TestTask(project)
			prepareTrx2XmlTask(project)
		}
	}



	private void prepareWp7TestTask(Project project) {
		def task = project.task('testWp7')
		task.description = "Build and executes  tests"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_TEST
		task << {
			projectHelper.executeCommand(project, projectTestDirectory, [
				'MSBuild',
				"/target:test",
				"/p:TestResultsFile=\""+project.name+".trx\""
			])

			trx2xml(project)
		}
	}

	private void prepareTrx2XmlTask(Project project) {
		def task = project.task('trx2xml')
		task.description = "Transforms mstest *.trx output files to junit/nuint xml"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_TEST
		task << {
			trx2xml(project)
		}
	}

	private void trx2xml(Project project) {
		logger.lifecycle("convert trx2xml")

		// TODO out
		String outputPath = "Bin/Debug/";

		File trxInput = new File(projectTestDirectory, outputPath+project.name+".trx")
		File xmlOutput  = new File(projectTestDirectory,outputPath+project.name+".xml")
		TrxToXmlTransformer transformer = new TrxToXmlTransformer()
		transformer.transform(trxInput, xmlOutput)
	}

	static public final String DESCRIPTION =
	"""This plugin provides easy automated testing framework for Windows Phone applications
	"""
}
