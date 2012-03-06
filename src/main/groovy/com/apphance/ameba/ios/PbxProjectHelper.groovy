package com.apphance.ameba.ios

import groovy.json.JsonSlurper;

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.GradleException
import groovy.io.FileType

class PbxProjectHelper {

	static Logger logger = Logging.getLogger(PbxProjectHelper.class)

	Object rootObject
	private int hash = 0
	boolean hasApphance = false

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
				logger.lifecycle("Framework already added")
				// apphance already added
				foundFramework = true
				if (name.equals("apphance")) {
					hasApphance = true
				}
				return
			}
		}
		return foundFramework
	}

	void addApphanceToFramework(Object frameworks) {
		def frameworksToAdd = [ ["name":"Apphance-iOS.framework", "path":"Apphance-iOS.framework", "group":"<group>", "searchName":"apphance", "strong":"Required"],
								["name":"CoreLocation.framework", "path":"System/Library/Frameworks/CoreLocation.framework", "group":"SDKROOT", "searchName":"corelocation.framework", "strong":"Required"],
								["name":"QuartzCore.framework", "path":"System/Library/Frameworks/QuartzCore.framework", "group":"SDKROOT", "searchName":"quartzcore.framework", "strong":"Required"],
								["name":"SystemConfiguration.framework", "path":"System/Library/Frameworks/SystemConfiguration.framework", "group":"SDKROOT", "searchName":"systemconfiguration.framework", "strong":"Weak"],
								["name":"CoreTelephony.framework", "path":"System/Library/Frameworks/CoreTelephony.framework", "group":"SDKROOT", "searchName":"coretelephony.framework", "strong":"Weak"],
								["name":"AudioToolbox.framework", "path":"System/Library/Frameworks/AudioToolbox.framework", "group":"SDKROOT", "searchName":"audiotoolbox.framework", "strong":"Required"]]

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

	void addFlagsAndPathsToProject(Object project, String configurationName) {
		def configurationList = getObject(project.buildConfigurationList)
		configurationList.buildConfigurations.each { configuration ->
			if (getObject(configuration).name.equals(configurationName)) {
				if (getObject(configuration).buildSettings.OTHER_LDFLAGS == null) {
					getObject(configuration).buildSettings.put("OTHER_LDFLAGS", [])
				}
				getObject(configuration).buildSettings.OTHER_LDFLAGS.add("-ObjC")
				getObject(configuration).buildSettings.OTHER_LDFLAGS.add("-all_load")
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

	String findAppDelegateFile(File projectRootDirectory) {
		String appFilename = ""
		projectRootDirectory.eachFileRecurse(FileType.FILES) {
			if (it.name.endsWith(".h") && it.text.contains("UIApplicationDelegate")) {
				appFilename = it.canonicalPath
				logger.lifecycle("Application delegate found in file " + it)
				return
			}
		}
		return appFilename
	}

	void addApphanceInit(File projectRootDirectory, String appKey) {
		def launchingPattern = /(application.*didFinishLaunchingWithOptions[^\n]*\s+\{)/
		def initApphance = "[APHLogger startNewSessionWithApplicationKey:@\"" + "${appKey}" + "\" apphanceMode:kAPHApphanceModeQA];"
		def setExceptionHandler = "NSSetUncaughtExceptionHandler(&APHUncaughtExceptionHandler);"
		String appFilename = findAppDelegateFile(projectRootDirectory)
		if (appFilename.equals("")) {
			throw new GradleException("Cannot find file with UIApplicationDelegate")
		}
		appFilename = appFilename.replace(".h", ".m")
		File appDelegateFile = new File(appFilename)

		File newAppDelegate = new File("newAppDelegate.m")
		newAppDelegate.delete()
		newAppDelegate.withWriter { out ->
			 out << appDelegateFile.text.replaceAll(launchingPattern, "\$1"+initApphance+setExceptionHandler)
		}
		appDelegateFile.delete()
		appDelegateFile.withWriter { out ->
			out << newAppDelegate.text
		}
	}

	void addApphanceToPch(File pchFile) {
		logger.lifecycle("Adding apphance header to file " + pchFile)
		File newPch = new File("newPch.pch")
		newPch.delete()
		newPch.withWriter { out ->
			out << pchFile.text.replace("#ifdef __OBJC__", "#ifdef __OBJC__\n#import <Apphance-iOS/APHLogger.h>")
		}
		pchFile.delete()
		pchFile.withWriter { out ->
			out << newPch.text
		}
	}

	String addApphanceToProject(File projectRootDirectory, String targetName, String configurationName, String appKey) {
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
				if (!hasApphance) {
					// Find pch file with proper configuration
					getObject(getObject("${target}").buildConfigurationList).buildConfigurations.each { configuration ->
						if (getObject(configuration).name.equals(configurationName)) {
							addApphanceToPch(new File(projectRootDirectory, getObject(configuration).buildSettings.GCC_PREFIX_HEADER))
						}
					}
				}
			}
		}
		if (!hasApphance) {
			addFlagsAndPathsToProject(project, configurationName)
			addApphanceInit(projectRootDirectory, appKey)
		}

		return writePlistToString()
	}
}
