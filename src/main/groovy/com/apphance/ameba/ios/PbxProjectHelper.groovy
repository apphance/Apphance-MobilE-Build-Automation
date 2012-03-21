package com.apphance.ameba.ios

import groovy.io.FileType
import groovy.xml.XmlUtil

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectHelper;

class PbxProjectHelper {

    static Logger logger = Logging.getLogger(PbxProjectHelper.class)

    Object rootObject
    Object objects
    private int hash = 0
    boolean hasApphance = false
    def projectFile = null

    Object getProperty(Object object, String propertyName) {
        def returnObject = null
        def property = object.key.each {
            if (it.text().equals(propertyName)) {
                returnObject = it
            }
        }
        if (returnObject != null) {
            returnObject = getNextNode(returnObject)
        }
        return returnObject
    }

    void setRootObject(Object rootObject) {
        this.rootObject = rootObject
    }

    Object getObjectsList() {
        def objects = null
        rootObject.dict.key.each {
            if (it.text().equals("objects")) {
                objects = it
                return
            }
        }
        objects = getNextNode(objects)
        return objects
    }

    Object getObject(String objectName) {
        def returnObject = null
        def objects = getObjectsList()
        objects.key.each {
            if (it.text().equals(objectName)) {
                returnObject = it
                return
            }
        }

        returnObject = getNextNode(returnObject)
        return returnObject
    }

    def getNextNode(Object object) {
        List list = object.parent().children()
		Iterator iter = list.iterator()
        while (iter.hasNext()) {
            def obj = iter.next()
            if (object == obj) {
                return iter.next()
            }
        }
        return null
    }

    def getParsedProject(File projectRootDirectory, String targetName) {
        projectRootDirectory.traverse([type: FileType.FILES, maxDepth : ProjectHelper.MAX_RECURSION_LEVEL]) {
            if (it.name.equals("project.pbxproj")) {
                projectFile = it
                return
            }
        }
        if (projectFile == null) {
            throw new GradleException("There is no project file in directory " + projectRootDirectory.canonicalPath + " with name " + targetName)
        }
        def command = ["plutil", "-convert", "xml1", "-o", "-", "${projectFile}"]
        Process proc = Runtime.getRuntime().exec((String[]) command.toArray())
        StringBuffer outBuff = new StringBuffer()
        proc.waitForProcessOutput(outBuff, null)
        XmlParser parser = new XmlParser(false, false)
        def root = parser.parseText(outBuff.toString())
        return root
    }

    int nextHash() {
        return hash++
    }

	void appendNodeWithKey(Object object, String key, String keyValue, String value, String valueString) {
		object.appendNode(key, keyValue)
		object.appendNode(value, valueString)
	}

    void addFramework(Object frameworks, String name, String path, String group, String strongWeak) {
        int apphanceFrameworkHash = nextHash()
        int apphanceFileFrameworkHash = nextHash()
        def objectsList = getObjectsList()

		objectsList.appendNode("key", apphanceFrameworkHash.toString())
		def dict = objectsList.appendNode("dict")
		appendNodeWithKey(dict, "key", "isa", "string", "PBXBuildFile")
		appendNodeWithKey(dict, "key", "fileRef", "string", apphanceFileFrameworkHash.toString())
		dict.appendNode("key", "settings")
		def settings = dict.appendNode("dict")
		settings.appendNode("key", "ATTRIBUTES")
		def attributes = settings.appendNode("array")
		attributes.appendNode("string", strongWeak)

		getProperty(frameworks, "files").appendNode("string", apphanceFrameworkHash.toString())
		objectsList.appendNode("key", apphanceFileFrameworkHash.toString())
		dict = objectsList.appendNode("dict")
		appendNodeWithKey(dict, "key", "isa", "string", "PBXFileReference")
		appendNodeWithKey(dict, "key", "lastKnownFileType", "string", "wrapper.framework")
		appendNodeWithKey(dict, "key", "name", "string", name)
		appendNodeWithKey(dict, "key", "path", "string", path)
		appendNodeWithKey(dict, "key", "sourceTree", "string", group)

        def project = getObject(getProperty(rootObject.dict, "rootObject").text())
        def mainGroupProp = getProperty(project, "mainGroup")
        def mainGroup = getObject(mainGroupProp.text())
		getProperty(mainGroup, "children").appendNode("string", apphanceFileFrameworkHash.toString())
    }

    boolean findFramework(Object frameworks, String name) {
        boolean foundFramework = false
        getProperty(frameworks, "files").'*'.each { file ->
            // find file reference in objects
            def fileString = file.text()
            def fileRef = getProperty(getObject("${fileString}"), "fileRef").text()
            def frameworkName = getProperty(getObject("${fileRef}"), "name")
            def frameworkPath = getProperty(getObject("${fileRef}"), "path")
            if ((frameworkName != null && frameworkName.text().toLowerCase().contains(name)) || (frameworkPath != null && frameworkPath.text().toLowerCase().endsWith(name))) {
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
        if (node.name().equals("dict")) {
            builder << "{\n"
            builder << "\t"*level
            node.key.each {
                    builder << "\"${it.text()}\" = "
                    builder << printElementToString(getNextNode(it), level + 1)
                    builder << ";\n"
                    builder << "\t"*level
            }
            builder << "}"
        } else if (node.name().equals("array")){
            // its list or array
            builder << "(\n"
            builder << "\t"*level
            node.string.each {
                builder << printElementToString(it, level + 1)
                builder << ",\n"
                builder << "\t"*level
            }
            builder << ")"
        } else {
            String nodeString = node.text()
            nodeString = nodeString.replace("\"", "\\\"")
            def lines = nodeString.split("\\r?\\n")
            if (lines.size() > 1) {
                builder << "\""
                lines.each {
                    builder << "${it}\\n"
                }
                builder << "\""
            } else {
                builder << "\"${nodeString}\""
            }

        }
        String s = builder.toString()
        return s
    }

    String writePlistToString() {
        StringBuilder builder = new StringBuilder()
        builder << printElementToString(rootObject.dict[0], 1)
        return builder.toString() + "\n"
    }

    void addFlagsAndPathsToProject(Object project, String configurationName) {
        def configurationList = getObject(getProperty(project, "buildConfigurationList").text())
        getProperty(configurationList, "buildConfigurations").'*'.each {
            def configuration = getObject(it.text())
            if (getProperty(configuration, "name").text().equals(configurationName)) {
                def buildSettings = getProperty(configuration, "buildSettings")
                def ldflags = getProperty(buildSettings, "OTHER_LDFLAGS")
                if (ldflags == null) {
					logger.lifecycle("Before adding " + buildSettings.children().size())
					buildSettings.appendNode("key", "OTHER_LDFLAGS")
					def array = buildSettings.appendNode("array")
					array.appendNode("string", "-ObjC")
					array.appendNode("string", "-all_load")
                } else {
					ldflags.appendNode("string", "-ObjC")
					ldflags.appendNode("string", "-all_load")
                }

                def frameworkSearchPaths = getProperty(buildSettings, "FRAMEWORK_SEARCH_PATHS")
                if (frameworkSearchPaths == null) {
					buildSettings.appendNode("key", "FRAMEWORK_SEARCH_PATHS")
					def array = buildSettings.appendNode("array")
					array.appendNode("string", "\$(inherited)")
					array.appendNode("string", "\$(SRCROOT)/")
                } else {
					frameworkSearchPaths.appendNode("string", "\$(SRCROOT)/")
                }

                def librarySearchPaths = getProperty(buildSettings, "LIBRARY_SEARCH_PATHS")
                if (librarySearchPaths == null) {
					buildSettings.appendNode("key", "LIBRARY_SEARCH_PATHS")
					def array = buildSettings.appendNode("array")
					array.appendNode("string", "\$(inherited)")
					array.appendNode("string", "\$(SRCROOT)/Apphance-iOS.framework")
                } else {
					librarySearchPaths.appendNode("string", "\$(SRCROOT)/Apphance-iOS.framework")
                }

            }
        }
    }

    String findAppDelegateFile(File projectRootDirectory) {
        String appFilename = ""
        projectRootDirectory.traverse([type: FileType.FILES, maxDepth : ProjectHelper.MAX_RECURSION_LEVEL]) {
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
        newAppDelegate.delete()
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
        newPch.delete()
    }

    String addApphanceToProject(File projectRootDirectory, String targetName, String configurationName, String appKey) {
        rootObject = getParsedProject(projectRootDirectory, targetName)
        def project = getObject(getProperty(rootObject.dict, "rootObject").text())
        getProperty(project, "targets").'*'.each { target ->
            def targetText = target.text()
            if (getProperty(getObject("${targetText}"), "name").text().equals(targetName)) {
                // find build phases in target
                getProperty(getObject("${targetText}"), "buildPhases").'*'.each { phase ->
                    // find frameworks in build phases
                    def phaseText = phase.text()
                    if (getProperty(getObject("${phaseText}"), "isa").text().equals("PBXFrameworksBuildPhase")) {
                        addApphanceToFramework(getObject("${phaseText}"))
                    }
                }
                if (!hasApphance) {
                    // Find pch file with proper configuration
                    def buildConfigurationList = getProperty(getObject("${targetText}"), "buildConfigurationList")
                    getProperty(getObject(buildConfigurationList.text()), "buildConfigurations").'*'.each { configuration ->
                        if (getProperty(getObject(configuration.text()), "name").text().equals(configurationName)) {
                            addApphanceToPch(new File(projectRootDirectory, getProperty(getProperty(getObject(configuration.text()), "buildSettings"),
                                "GCC_PREFIX_HEADER").text()))
                        }
                    }
                }
            }
        }
        if (!hasApphance) {
            addFlagsAndPathsToProject(project, configurationName)
            addApphanceInit(projectRootDirectory, appKey)
        }

        File f = new File(projectRootDirectory, "newProject.pbxproj")
        f.delete()
        f.withWriter { writer ->
            writer << writePlistToString()
        }
        projectFile.withWriter { out ->
            out << f.text
        }
        f.delete()
        return projectFile.text
    }
}
