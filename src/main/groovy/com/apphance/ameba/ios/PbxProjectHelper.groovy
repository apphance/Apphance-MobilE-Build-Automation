package com.apphance.ameba.ios

import groovy.json.JsonSlurper;

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.GradleException

class PbxProjectHelper {

	static Logger logger = Logging.getLogger(PbxProjectHelper.class)

	private int hash = 0

	def getParsedProject(File projectRootDirectory, String targetName) {
		File projectFile = new File(projectRootDirectory, "${targetName}.xcodeproj/project.pbxproj")
		if (!projectFile.exists()) {
			throw new GradleException("There is no project file in directory " + projectRootDirectory.canonicalPath + " with name " + targetName)
		}
		def command = ["plutil", "-convert", "json", "-o", "-", "${projectFile}"]
		Process proc = Runtime.getRuntime().exec((String[]) command.toArray())
		proc.waitFor()
		// get output
		StringBuffer outBuff = new StringBuffer()
		Thread t = proc.consumeProcessOutputStream(outBuff)
		t.join()
		JsonSlurper slurper = new JsonSlurper()
		return slurper.parseText(outBuff.toString())
	}

	int nextHash() {
		return hash++
	}

	void addApphanceToFramework(Object rootObject, Object frameworks) {
		boolean foundApphance = false
		frameworks.files.each { file ->
			// find file reference in objects
			def fileRef = rootObject.objects."${file}".fileRef
			if (rootObject.objects."${fileRef}".name.toLowerCase().contains("apphance")) {
				logger.lifecycle("Apphance already added")
				// apphance already added
				foundApphance = true
				return
			}
		}
		if (foundApphance) {
			return
		}
		logger.lifecycle("Apphance not found")
		int apphanceFrameworkHash = nextHash()
		int apphanceFileFrameworkHash = nextHash()
		rootObject.objects.put(apphanceFrameworkHash.toString(), [isa : "PBXBuildFile", fileRef : apphanceFileFrameworkHash])
		frameworks.files.add(apphanceFrameworkHash.toString())
		rootObject.objects.put(apphanceFileFrameworkHash.toString(), [name : "Apphance-iOS.framework"])
	}

	void addApphanceToProject(File projectRootDirectory, String targetName) {
		def rootObject = getParsedProject(projectRootDirectory, targetName)
		def project = rootObject.objects."${rootObject.rootObject}"
		project.targets.each { target ->
			if (rootObject.objects."${target}".name.equals(targetName)) {
				// find build phases in target
				rootObject.objects."${target}".buildPhases.each { phase ->
					// find frameworks in build phases
					if (rootObject.objects."${phase}".isa.equals("PBXFrameworksBuildPhase")) {
						addApphanceToFramework(rootObject, rootObject.objects."${phase}")
					}
				}
			}
		}
	}
}
