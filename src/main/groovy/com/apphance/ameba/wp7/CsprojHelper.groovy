package com.apphance.ameba.wp7

import java.io.File

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectConfiguration


/**
 * @author Gocal
 */
class CsprojHelper {

	static Logger logger = Logging.getLogger(CsprojHelper.class)

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

	void readVersionFromWMAppManifest(String WMAppManifestPath, ProjectConfiguration conf) {
		def WMAppManifest = new File("${WMAppManifestPath}")

		def xmlSlurper = new XmlSlurper()
		def xml = xmlSlurper.parse(WMAppManifest)
		conf.versionString = xml.WMAppManifest.App("Version")
	}
}
