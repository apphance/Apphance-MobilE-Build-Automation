package com.apphance.ameba.wp7

import java.io.File

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectConfiguration


/**
 * @author Gocal
 */
class Wp7ProjectHelper {

	static Logger logger = Logging.getLogger(Wp7ProjectHelper.class)

	String getCsprojName(File projectDir) {


		String[] files = projectDir.list(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".csproj");
					}
				});

		if(files == null || files.length == 0) {
			return null;
		}

		if(files.length > 1) {
			logger.debug("Project contains more than one *.csproj file")
		}

		return files[0];
	}

	File getSolutionFile(File projectDir) {

		FilenameFilter slnFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".sln");
			}
		};

		String[] projectFiles = projectDir.list(slnFilter);
		if(projectFiles != null && projectFiles.length > 0) {
			return new File(projectDir, projectFiles[0]);
		}

		File parentDir = projectDir.getParentFile();
		String[] parentFiles = parentDir.list(slnFilter);
		if(parentFiles != null && parentFiles.length > 0) {
			return new File(projectDir, parentFiles[0]);
		}

		return null;
	}

	void readConfigurationsFromSln(File slnFile, Wp7ProjectConfiguration wp7conf) {
		wp7conf.targets = ['Phone', 'Emulator']
		wp7conf.configurations = ['Debug', 'Release']
	}

	void readVersionFromWMAppManifest(String WMAppManifestPath, ProjectConfiguration conf) {
		def WMAppManifest = new File("${WMAppManifestPath}")

		def xmlSlurper = new XmlSlurper()
		def xml = xmlSlurper.parse(WMAppManifest)
		conf.versionString = xml.WMAppManifest.App("Version")
	}
}
