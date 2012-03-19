package com.apphance.ameba.wp7

import java.io.File

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectConfiguration
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;


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
			return new File(parentDir, parentFiles[0]);
		}

		return null;
	}

	void readConfigurationsFromSln(File slnFile, Wp7ProjectConfiguration wp7conf) {

		wp7conf.targets = ['Device', 'Emulator'] // in future version of wp7 sdk we'll have multiple targets - 256MB/512MB devices

		if(slnFile == null) {
			wp7conf.targets = ['AnyCPU']
			wp7conf.configurations = ['Debug', 'Release']
			return
		}

		List<String> configurations = new ArrayList<String>();
		List<String> targets = new ArrayList<String>();

		BufferedReader reader = new BufferedReader(new FileReader(slnFile));
		String line;
		while((line = reader.readLine()) != null) {

			if(line.contains("GlobalSection(SolutionConfigurationPlatforms)")) {
				while((line = reader.readLine()) != null && !line.contains("EndGlobalSection")) {
					// TODO use regex groups
					def trimmed=line.trim()

					int sep = trimmed.indexOf('|')
					def configuration = trimmed.substring(0, sep)
					if(!configurations.contains(configuration)) {
						configurations.add(configuration)
					}

					def target = trimmed.substring(sep+1, trimmed.indexOf("=")).trim().replace(" ", "")
					if(!targets.contains(target)) {
						targets.add(target)
					}
				}
			}
		}
		wp7conf.targets = targets.toArray(new String[targets.size()]);
		wp7conf.configurations = configurations.toArray(new String[configurations.size()]);
	}

	void readVersionFromWMAppManifest(File WMAppManifest, ProjectConfiguration conf) {
		def xmlSlurper = new XmlSlurper()
		def Deployment = xmlSlurper.parse(WMAppManifest)
		conf.versionString = Deployment.App[0].@Version.text()
	}
}
