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
		StringBuffer outBuff = new StringBuffer()
		proc.waitForProcessOutput(outBuff, null)
		JsonSlurper slurper = new JsonSlurper()
		return slurper.parseText(outBuff.toString())
	}

	int nextHash() {
		return hash++
	}

	void addFramework(Object frameworks, String name, String path, String group, String strongWeak) {
		int apphanceFrameworkHash = nextHash()
		int apphanceFileFrameworkHash = nextHash()
		rootObject.objects.put(apphanceFrameworkHash.toString(), [isa : "PBXBuildFile", fileRef : apphanceFileFrameworkHash, settings : [ATTRIBUTES:[strongWeak,]]])
		frameworks.files.add(apphanceFrameworkHash.toString())
		rootObject.objects.put(apphanceFileFrameworkHash.toString(), [isa : "PBXFileReference", lastKnownFileType : "wrapper.framework", name : name, path : path, sourceTree : group ])

		def mainGroup = getObject(getObject(rootObject.rootObject).mainGroup)
		mainGroup.children.add(apphanceFileFrameworkHash.toString())
	}

	boolean findFramework(Object frameworks, String name) {
		boolean foundFramework = false
		frameworks.files.each { file ->
			// find file reference in objects
			def fileRef = getObject("${file}").fileRef
			if (getObject("${fileRef}").name.toLowerCase().contains(name)) {
				logger.lifecycle("Apphance already added")
				// apphance already added
				foundFramework = true
				return
			}
		}
		return foundFramework
	}

	void addApphanceToFramework(Object frameworks) {
		def frameworksToAdd = [ ["name":"Apphance-iOS.framework", "path":"Apphance-iOS.framework", "group":"<group>", "searchName":"apphance", "strong":"Required"],
								["name":"CoreLocation.framework", "path":"System/Library/Frameworks/CoreLocation.framework", "group":"SDKROOT", "searchName":"CoreLocation.framework", "strong":"Required"],
								["name":"QuartzCore.framework", "path":"System/Library/Frameworks/QuartzCore.framework", "group":"SDKROOT", "searchName":"QuartzCore.framework", "strong":"Required"],
								["name":"SystemConfiguration.framework", "path":"System/Library/Frameworks/SystemConfiguration.framework", "group":"SDKROOT", "searchName":"SystemConfiguration.framework", "strong":"Weak"],
								["name":"CoreTelephony.framework", "path":"System/Library/Frameworks/CoreTelephony.framework", "group":"SDKROOT", "searchName":"CoreTelephony.framework", "strong":"Weak"],
								["name":"AudioToolbox.framework", "path":"System/Library/Frameworks/AudioToolbox.framework", "group":"SDKROOT", "searchName":"AudioToolbox.framework", "strong":"Required"]]

		frameworksToAdd.each { framework ->
			if (findFramework(frameworks, framework["searchName"])) {
				logger.lifecycle("Framework " + framework["searchName"] + " already in project")
				return
			}
			logger.lifecycle("Framework " + framework["searchName"] + " not found in project")
			addFramework(frameworks, framework["name"], framework["path"], framework["group"], framework["strong"])
		}

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
		} else {
			String nodeString = node.toString()
			nodeString = nodeString.replace("\"", "\\\"")
			def lines = nodeString.split("\\r?\\n")
			if (lines.size() > 1) {
				builder << "\""
				lines.each {
					builder << "${it}\\n"
				}
				builder << "\""
			} else {
				if (!nodeString.startsWith("\"")) {
					builder << "\"${node}\""
				} else {
					builder << "${node}"
				}
			}

		}
		String s = builder.toString()
		return s
	}

	String writePlistToString() {
		StringBuilder builder = new StringBuilder()
		builder << printElementToString(rootObject, 1)
		return builder.toString() + "\n"
	}

	void addFlagsAndPathsToProject(Object project) {
		def configurationList = getObject(project.buildConfigurationList)
		configurationList.buildConfigurations.each { configuration ->
			if (getObject(configuration).name.equals(configurationName)) {
				if (getObject(configuration).OTHER_LDFLAGS == null) {
					getObject(configuration).put("OTHER_LDFLAGS", [])
				}
				getObject(configuration).OTHER_LDFLAGS.add("-ObjC")
				getObject(configuration).OTHER_LDFLAGS.add("-all_load")
				if (getObject(configuration).buildSettings.FRAMEWORK_SEARCH_PATHS == null) {
					getObject(configuration).buildSettings.put("FRAMEWORK_SEARCH_PATHS", ["\$(inherited)"])
				}
				getObject(configuration).buildSettings.FRAMEWORK_SEARCH_PATHS.add("\$(SRCROOT)/")
				if (getObject(configuration).buildSettings.LIBRARY_SEARCH_PATHS == null) {
					getObject(configuration).buildSettings.put("LIBRARY_SEARCH_PATHS", ["\$(inherited)"])
				}
				getObject(configuration).buildSettings.LIBRARY_SEARCH_PATHS.add("\$(SRCROOT)/Apphance-iOS.framework")

			}
		}
	}

	void addApphanceInit(File projectRootDirectory) {
		def launchingPattern = /application.*didFinishLaunchingWithOptions[^\(\r\n]*\s+\{)/
		
	}

	String addApphanceToProject(File projectRootDirectory, String targetName, String configurationName) {
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
		addFlagsAndPathsToProject(project)
		addApphanceInit(projectRootDirectory)

		logger.lifecycle(writePlistToString())
		return writePlistToString()
	}
}
