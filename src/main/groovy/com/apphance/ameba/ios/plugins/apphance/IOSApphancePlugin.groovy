package com.apphance.ameba.ios.plugins.apphance

import java.util.zip.ZipFile;

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectConfiguration;
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.apphance.PrepareApphanceSetupOperation
import com.apphance.ameba.apphance.ShowApphancePropertiesOperation
import com.apphance.ameba.apphance.VerifyApphanceSetupOperation
import com.apphance.ameba.apphance.ApphanceProperty;
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser;
import com.apphance.ameba.ios.PbxProjectHelper;
import com.apphance.ameba.ios.plugins.buildplugin.IOSPlugin

class IOSApphancePlugin implements Plugin<Project> {

	static Logger logger = Logging.getLogger(IOSApphancePlugin.class)

	ProjectHelper projectHelper
	ProjectConfiguration conf
	IOSProjectConfiguration iosConf
	IOSXCodeOutputParser iosXcodeOutputParser
	PbxProjectHelper pbxProjectHelper

	public void apply(Project project) {
		ProjectHelper.checkAllPluginsAreLoaded(project, this.class, IOSPlugin.class)
		use (PropertyCategory) {
			this.projectHelper = new ProjectHelper()
			this.conf = project.getProjectConfiguration()
			this.iosXcodeOutputParser = new IOSXCodeOutputParser()
			this.iosConf = iosXcodeOutputParser.getIosProjectConfiguration(project)
			this.pbxProjectHelper = new PbxProjectHelper()

			def trimmedListOutput = projectHelper.executeCommand(project, ["xcodebuild", "-list"]as String[],false, null, null, 1, true)*.trim()
			iosConf.configurations = iosXcodeOutputParser.readBuildableConfigurations(trimmedListOutput)
			iosConf.targets = iosXcodeOutputParser.readBuildableTargets(trimmedListOutput)
			preprocessBuildsWithApphance(project)

			project.prepareSetup.prepareSetupOperations << new PrepareApphanceSetupOperation()
			project.verifySetup.verifySetupOperations << new VerifyApphanceSetupOperation()
			project.showSetup.showSetupOperations << new ShowApphancePropertiesOperation()
		}
	}

	void preprocessBuildsWithApphance(Project project) {
		iosConf.configurations.each { configuration ->
			iosConf.targets.each { target ->
				if (!iosConf.isBuildExcluded(target + "-" + configuration)) {
					project."build-${target}-${configuration}".doFirst {
						if (!isApphancePresent(new File(project.rootDir, iosConf.tmpDirName(target, configuration)))) {
							replaceLogsWithApphance(project, iosConf.tmpDirName(target, configuration))
							pbxProjectHelper.addApphanceToProject(new File(project.rootDir, iosConf.tmpDirName(target, configuration)), target, configuration, project[ApphanceProperty.APPLICATION_KEY.propertyName])
							copyApphanceFramework(project, iosConf.tmpDirName(target, configuration))
						}
					}
				}
			}
		}
	}

	void replaceLogsWithApphance(Project project, String tmpDir) {
		logger.lifecycle("Replacing android logs with apphance")
		project.ant.replace(casesensitive: 'true', token : 'NSLog',
				value: 'APHLog', summary: true) {
					fileset(dir: "${project.rootDir}/" + tmpDir) { include (name : '**/*.m') }
				}
	}

	private copyApphanceFramework(Project project, String tmpDir) {
		def libsDir = new File(project.rootDir, tmpDir)
		logger.lifecycle("Copying apphance into directory " + libsDir)
		libsDir.mkdirs()
		libsDir.eachFileRecurse { framework ->
			if (framework == ".*[aA]pphance.*\\.framework") {
				logger.lifecycle("Removing old apphance framework: " + framework.name)
				def delClos = {
					it.eachDir( delClos );
					it.eachFile {
						it.delete()
					}
				}

				// Apply closure
				delClos( new File(framework.canonicalPath) )
			}
		}

		InputStream apphanceZip = this.class.getResourceAsStream("Apphance-iOS.framework.zip")
		def projectApphanceZip = new File(libsDir, "apphance.zip")
		projectApphanceZip.delete()
		projectApphanceZip.withWriter{ out ->
			out << apphanceZip.getText()
		}

		logger.lifecycle("Unpacking file " + projectApphanceZip)
		logger.lifecycle("Exists " + projectApphanceZip.exists())
		def command = ["unzip", "${projectApphanceZip}", "-d", "${libsDir}"]
		Process proc = Runtime.getRuntime().exec((String[]) command.toArray())
		proc.waitFor()
	}

	boolean isApphancePresent(File projectDir) {
		def apphancePresent = false

		projectDir.eachFileRecurse { framework ->
			if (framework =~ ".*[aA]pphance.*\\.framework") {
				apphancePresent = true
			}
		}

		if (apphancePresent) {
			logger.lifecycle("Apphance already in project")
		} else {
			logger.lifecycle("Apphance not in project")
		}
		return apphancePresent
	}
}
