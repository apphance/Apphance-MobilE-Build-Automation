package com.apphance.ameba.wp7.plugins.apphance

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.plugins.apphance.ApphanceProperty
import com.apphance.ameba.android.plugins.apphance.PrepareApphanceSetupOperation
import com.apphance.ameba.android.plugins.apphance.ShowApphancePropertiesOperation
import com.apphance.ameba.android.plugins.apphance.VerifyApphanceSetupOperation
import com.apphance.ameba.wp7.Wp7ProjectConfiguration
import com.apphance.ameba.wp7.Wp7ProjectHelper
import com.apphance.ameba.wp7.plugins.buildplugin.Wp7ProjectProperty

class Wp7ApphancePlugin implements Plugin<Project> {

	private static String APPHANCE_DLL_NAME = "Apphance.WindowsPhone.dll";

	static Logger logger = Logging.getLogger(Wp7ApphancePlugin.class)

	ProjectConfiguration conf
	Wp7ProjectConfiguration wp7Conf;
	ProjectHelper projectHelper
	Wp7ProjectHelper wp7ProjectHelper;
	ApphanceSourceCodeHelper sourceHelper;

	public void apply(Project project) {
		use (PropertyCategory) {
			this.projectHelper = new ProjectHelper()
			this.conf = project.getProjectConfiguration()
			this.wp7Conf = new Wp7ProjectConfiguration()
			this.wp7ProjectHelper = new Wp7ProjectHelper()
			this.sourceHelper = new ApphanceSourceCodeHelper()

			preprocessBuildsWithApphance(project)

			project.prepareSetup.prepareSetupOperations << new PrepareApphanceSetupOperation()
			project.verifySetup.verifySetupOperations << new VerifyApphanceSetupOperation()
			project.showSetup.showSetupOperations << new ShowApphancePropertiesOperation()
		}
	}


	void preprocessBuildsWithApphance(Project project) {

		File slnFile = wp7ProjectHelper.getSolutionFile(project.rootDir)
		wp7ProjectHelper.readConfigurationsFromSln(slnFile, wp7Conf)


		wp7Conf.configurations.each { configuration ->
			wp7Conf.targets.each { target ->

				project."build${target}${configuration}".doFirst {
					//if ("Debug".equals(configuration))
					//{
					use (PropertyCategory) {

						File variantDir = wp7Conf.getVariantDirectory(project, target, configuration)

						String appId = project.readProperty(Wp7ProjectProperty.APPHANCE_APPLICATION_KEY)
						extractApphanceDll(variantDir)
						addApphaceToCsProj(variantDir)
						String appKey = project[ApphanceProperty.APPLICATION_KEY.propertyName]
						addApphanceToAppCs(variantDir, appKey)
						convertsSystemDebugToApphanceLogs(variantDir)
					//}

					//replaceLogsWithApphance(project, iosConf.tmpDirName(target, configuration))
					//pbxProjectHelper.addApphanceToProject(new File(project.rootDir, iosConf.tmpDirName(target, configuration)), target, configuration, project[ApphanceProperty.APPLICATION_KEY.propertyName])
					//copyApphanceFramework(project, iosConf.tmpDirName(target, configuration))
					}
				}
			}
		}
	}

	void extractApphanceDll(File projectDir) {
		sourceHelper.extractApphanceDll(projectDir, APPHANCE_DLL_NAME)
	}

	void  removeApphanceDll(File projectDir) {
		File apphanceDll = new File(projectDir, APPHANCE_DLL_NAME)
		apphanceDll.delete()
	}


	void addApphaceToCsProj(File projectDir) {
		logger.lifecycle("addApphaceToCsProj ${projectDir}")
		def csProj = new File(projectDir, wp7ProjectHelper.getCsprojName(projectDir))
		String csProjContent = csProj.text
		csProj.delete()
		csProj << sourceHelper.addApphanceToCsProj(csProjContent, APPHANCE_DLL_NAME)
	}

	void removeApphaceFromCsProject(File projectDir) {
		logger.lifecycle("removeApphaceFromCsProject ${projectDir}")
		def csProj = new File(projectDir, wp7ProjectHelper.getCsprojName(projectDir))
		String csProjContent = csProj.text
		csProj.delete()
		csProj << sourceHelper.removeApphanceFromCsProj(csProjContent, APPHANCE_DLL_NAME)
	}

	void addApphanceToAppCs(File projectDir, String appId) {
		logger.lifecycle("addApphanceToAppCs ${projectDir}")
		use (PropertyCategory) {
			wp7ProjectHelper.readVersionFromWMAppManifest(new File(projectDir, "Properties/WMAppManifest.xml"), conf)
			String version = conf.versionString
			def appCs = new File(projectDir, "App.xaml.cs")
			String appCsContent = appCs.text
			appCs.delete()
			appCs << sourceHelper.addApphanceToAppCs(appCsContent, appId, version)
		}
	}


	void removeApphanceFromAppCs(File projectDir) {
		logger.lifecycle("removeApphanceFromAppCs ${projectDir}")
		projectDir.eachFileMatch(~/.*cs/) { sourceFile ->
			String sourceFileContent = sourceFile.text
			sourceFile.delete()
			sourceFile << sourceHelper.removeApphanceFromAppCs(sourceFileContent)
		}
	}

	void convertsSystemDebugToApphanceLogs(File projectDir) {
		logger.lifecycle("convertsSystemDebugToApphanceLogs ${projectDir}")
		projectDir.eachFileMatch(~/.*cs/) { sourceFile ->
			String sourceFileContent = sourceFile.text
			sourceFile.delete()
			sourceFile << sourceHelper.convertSystemDebugToApphanceLogs(sourceFileContent)
		}
	}

	void convertsApphanceLogsToSystemDebug(File projectDir) {
		logger.lifecycle("convertsApphanceLogsToSystemDebug ${projectDir}")
		projectDir.eachFileMatch(~/.*cs/) { sourceFile ->
			String sourceFileContent = sourceFile.text
			sourceFile.delete()
			sourceFile << sourceHelper.convertApphanceLogsToSystemDebug(sourceFileContent)
		}
	}

	static public final String DESCRIPTION =
	"""This is the plugin that links Ameba with Apphance service.

The plugin provides integration with Apphance service. It performs the
following tasks: adding Apphance on-the-fly while building the application
(for all Debug builds), removing Apphance on-the-fly while building the application
(for all Release builds), submitting the application to apphance at release time.
"""
}
