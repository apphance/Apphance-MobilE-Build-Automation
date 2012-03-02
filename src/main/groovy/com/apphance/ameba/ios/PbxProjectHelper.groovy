package com.apphance.ameba.ios

import groovy.json.JsonSlurper;

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.GradleException

class PbxProjectHelper {

	static Logger logger = Logging.getLogger(PbxProjectHelper.class)

	Object rootObject
	private int hash = 0

	Object getObject(String objectName) {
		return rootObject.objects."${objectName}"
	}

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

	void addApphanceToFramework(Object frameworks) {
		boolean foundApphance = false
		frameworks.files.each { file ->
			// find file reference in objects
			def fileRef = getObject("${file}").fileRef
			if (getObject("${fileRef}").name.toLowerCase().contains("apphance")) {
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
		rootObject.objects.put(apphanceFileFrameworkHash.toString(), [isa : PBXFileReference, lastKnownFileType : wrapper.framework, name : "Apphance-iOS.framework", path : "Apphance-iOS.framework", sourceTree : "<group>" ])
		def mainGroup = getObject(project.mainGroup)
		mainGroup.children.add(apphanceFileFrameworkHash.toString())
	}

	String printElementToString(Object node, int level) {
		StringBuilder builder = new StringBuilder()
		if (node instanceof Map) {
			builder << "{\n"
			builder << "\t"*level
			node.each { key, value ->
				builder << "\"${key}\" = "
				builder << printElementToString(node[key], level + 1)
				builder << ";\n"
				builder << "\t"*level
			}
			builder << "}"
		} else if (node instanceof Collection){
			// its list or array
			builder << "(\n"
			builder << "\t"*level
			node.each {
				builder << printElementToString(it, level + 1)
				builder << ",\n"
				builder << "\t"*level
			}
			builder << ")"
//		} else if (node instanceof String) {
//			builder << "\"${node}\""
		} else {
			String nodeString = node.toString()
			if (nodeString.contains('$(SRCROOT)/GradleXCode')) {
				int a = 0;
			}
			def lines = nodeString.split("\\r?\\n")
			if (lines.size() > 1) {
				builder << "\""
				lines.each {
					builder << "${it}\\n"
				}
				builder << "\""
			} else {
				builder << "\"${node}\""
			}

		}
		return builder.toString()
	}

	String writePlistToString() {
		StringBuilder builder = new StringBuilder()
		builder << printElementToString(rootObject, 1)
		return builder.toString()
	}

	String addApphanceToProject(File projectRootDirectory, String targetName) {
		rootObject = getParsedProject(projectRootDirectory, targetName)
		def project = getObject("${rootObject.rootObject}")
		project.targets.each { target ->
			if (getObject("${target}").name.equals(targetName)) {
				// find build phases in target
				getObject("${target}").buildPhases.each { phase ->
					// find frameworks in build phases
					if (getObject("${phase}").isa.equals("PBXFrameworksBuildPhase")) {
						addApphanceToFramework(getObject("${phase}"))
					}
				}
			}
		}
		logger.lifecycle(rootObject.toString())
		logger.lifecycle(writePlistToString())
		return writePlistToString()
	}
}
