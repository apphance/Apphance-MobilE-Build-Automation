package com.apphance.ameba.ios.plugins.apphance

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
				project."build-${target}-${configuration}".doFirst {
					replaceLogsWithApphance(project)
					pbxProjectHelper.addApphanceToProject(new File(project.rootDir, "srcTmp"), target, configuration, project[ApphanceProperty.APPLICATION_KEY.propertyName])
					copyApphanceFramework(project)
				}
			}
		}
	}

	void replaceLogsWithApphance(Project project) {
		logger.lifecycle("Replacing android logs with apphance")
		project.ant.replace(casesensitive: 'true', token : 'NSLog',
				value: 'APHLog', summary: true) {
					fileset(dir: "${project.rootDir}/srcTmp/") { include (name : '**/*.m') }
				}
	}

	private copyApphanceFramework(Project project) {
		def libsDir = new File(project.rootDir, 'srcTmp/')
		logger.lifecycle("Copying apphance into directory " + libsDir)
		libsDir.mkdirs()
		libsDir.eachFileMatch(".*[aA]pphance.*\\.framework") { framework ->
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
		URL apphanceUrl = this.class.getResource("Apphance-iOS.framework.zip")
		copy {
			from zipFile(apphanceUrl)
			into "${project.rootDir}"
		}
	}
}
