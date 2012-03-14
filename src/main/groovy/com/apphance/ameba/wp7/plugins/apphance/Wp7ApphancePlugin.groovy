package com.apphance.ameba.wp7.plugins.apphance

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.plugins.apphance.PrepareApphanceSetupOperation
import com.apphance.ameba.android.plugins.apphance.ShowApphancePropertiesOperation
import com.apphance.ameba.android.plugins.apphance.VerifyApphanceSetupOperation
import com.apphance.ameba.wp7.Wp7ProjectHelper
import com.apphance.ameba.wp7.plugins.buildplugin.Wp7ProjectProperty

class Wp7ApphancePlugin implements Plugin<Project> {

	private static String APPHANCE_DLL_NAME = "Apphance.WindowsPhone.dll";

	static Logger logger = Logging.getLogger(Wp7ApphancePlugin.class)

	ProjectConfiguration conf
	ProjectHelper projectHelper
	Wp7ProjectHelper csprojHelper
	ApphanceSourceCodeHelper sourceHelper;

	public void apply(Project project) {
		use (PropertyCategory) {
			this.projectHelper = new ProjectHelper()
			this.conf = project.getProjectConfiguration()
			this.csprojHelper = new Wp7ProjectHelper()
			this.sourceHelper = new ApphanceSourceCodeHelper()

			prepareExtractApphanceDll(project)
			prepareRemoveApphanceDll(project)

			prepareAddApphaceToCsProj(project)
			prepareRemoveApphaceFromCsProject(project)

			prepareAddApphanceToAppCs(project)
			prepareRemoveApphanceFromAppCs(project)

			prepareConvertsSystemDebugToApphanceLogs(project)
			prepareConvertsApphanceLogsToSystemDebug(project)

			project.prepareSetup.prepareSetupOperations << new PrepareApphanceSetupOperation()
			project.verifySetup.verifySetupOperations << new VerifyApphanceSetupOperation()
			project.showSetup.showSetupOperations << new ShowApphancePropertiesOperation()
		}
	}

	void prepareExtractApphanceDll(Project project) {
		def task = project.task('extractApphanceDll')
		task.description = "Extract 'Apphance.WindowsPhone.dll' to project's directory"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
		task << {
			File projectDir = getProjectDir(project)
			sourceHelper.extractApphanceDll(projectDir, APPHANCE_DLL_NAME)
		}
	}

	void  prepareRemoveApphanceDll(Project project) {
		def task = project.task('removeApphanceDll')
		task.description = "Remove 'Apphance.WindowsPhone.dll' from project's directory"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
		task << {
			File projectDir = getProjectDir(project)
			File apphanceDll = new File(projectDir, APPHANCE_DLL_NAME)
			apphanceDll.delete()
		}
	}


	void prepareAddApphaceToCsProj(Project project) {
		def task = project.task('addApphanceToCsProj')
		task.description = "Add Apphance entries to *.csproj file"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
		task << {

			File projectDir = getProjectDir(project)
			def csProj = new File(csprojHelper.getCsprojName(projectDir))
			String csProjContent = csProj.text
			csProj.delete()
			csProj << sourceHelper.addApphanceToCsProj(csProjContent, APPHANCE_DLL_NAME)
		}
	}

	void prepareRemoveApphaceFromCsProject(Project project) {
		def task = project.task('removeApphanceFromCsProj')
		task.description = "Remove Apphance entries from *.csproj file"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
		task << {

			File projectDir = getProjectDir(project)
			def csProj = new File(csprojHelper.getCsprojName(projectDir))
			String csProjContent = csProj.text
			csProj.delete()
			csProj << sourceHelper.removeApphanceFromCsProj(csProjContent, APPHANCE_DLL_NAME)
		}
	}

	void prepareAddApphanceToAppCs(Project project) {
		use (PropertyCategory) {

			def task = project.task('addApphanceToAppCs')
			task.description = "Add Apphance entries to App.xaml.cs"
			task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
			task << {
				String appId = project.readProperty(Wp7ProjectProperty.APPHANCE_APPLICATION_KEY)
				csprojHelper.readVersionFromWMAppManifest("Properties/WMAppManifest.xml", conf)
				String version = conf.versionString

				File projectDir = getProjectDir(project)
				def appCs = new File(projectDir, "App.xaml.cs")
				String appCsContent = appCs.text
				appCs.delete()
				appCs << sourceHelper.addApphanceToAppCs(appCsContent, appId, version)
			}
		}
	}


	void prepareRemoveApphanceFromAppCs(Project project) {
		use (PropertyCategory) {
			def task = project.task('removeApphanceFromAppCs')
			task.description = "Remove Apphance entries from App.xaml.cs"
			task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
			task << {

				File projectDir = getProjectDir(project)

				projectDir.eachFileMatch(~/.*cs/) { sourceFile ->
					String sourceFileContent = sourceFile.text
					sourceFile.delete()
					sourceFileContent << sourceHelper.removeApphanceFromAppCs(projectDir)
				}
			}
		}
	}

	void prepareConvertsSystemDebugToApphanceLogs(Project project) {
		def task = project.task('convertLogsToApphance')
		task.description = "Converts all system.debug logs to Apphance logs"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
		task << {
			File projectDir = getProjectDir(project)

			projectDir.eachFileMatch(~/.*cs/) { sourceFile ->
				String sourceFileContent = sourceFile.text
				sourceFile.delete()
				sourceFileContent << sourceHelper.convertSystemDebugToApphanceLogs(projectDir)
			}
		}
	}

	void prepareConvertsApphanceLogsToSystemDebug(Project project) {
		def task = project.task('convertLogsToSystemDebug')
		task.description = "Converts all Apphance logs to system.debug logs"
		task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
		task << {
			File projectDir = getProjectDir(project)

			projectDir.eachFileMatch(~/.*cs/) { sourceFile ->
				String sourceFileContent = sourceFile.text
				sourceFile.delete()
				sourceFileContent << sourceHelper.convertApphanceLogsToSystemDebug(projectDir)
			}
		}
	}



	File getProjectDir(Project project) {
		return project.rootDir
	}

	static public final String DESCRIPTION =
	"""This is the plugin that links Ameba with Apphance service.

The plugin provides integration with Apphance service. It performs the
following tasks: adding Apphance on-the-fly while building the application
(for all Debug builds), removing Apphance on-the-fly while building the application
(for all Release builds), submitting the application to apphance at release time.
"""
}
