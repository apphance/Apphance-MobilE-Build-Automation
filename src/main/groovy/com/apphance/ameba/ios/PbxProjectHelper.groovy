package com.apphance.ameba.ios

import com.apphance.ameba.ios.plugins.apphance.IOSApphanceSourceHelper
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Helper parsing PBX project file.
 *
 */
class PbxProjectHelper {

    static Logger logger = Logging.getLogger(PbxProjectHelper.class)

    static final String PROJECT_PBXPROJ = "project.pbxproj"

    Object rootObject
    Object objects
    private int hash = 0
    boolean hasApphance = false
    def projectFile = null
    IOSApphanceSourceHelper apphanceSourceHelper = new IOSApphanceSourceHelper()

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

    def getParsedProject(File projectFile, String targetName) {
        def command = ["plutil", "-convert", "xml1", "-o", "-", projectFile.absolutePath]
        logger.info("Executing ${command}")
        Process proc = Runtime.getRuntime().exec((String[]) command.toArray())
        StringBuffer outBuff = new StringBuffer()
        proc.waitForProcessOutput(outBuff, null)
        XmlParser parser = new XmlParser(false, false)
        logger.debug("Received: ${outBuff}")
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
            def property = getProperty(getObject("${fileString}"), "fileRef")
            if (property != null) {
                def fileRef = property.text()
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
        }
        return foundFramework
    }

    void addApphanceToFramework(Object frameworks) {
        def frameworksToAdd = [
                ["name": "Apphance-Pre-Production.framework", "path": "Apphance-Pre-Production.framework", "group": "<group>", "searchName": "apphance", "strong": "Required"],
                ["name": "CoreLocation.framework", "path": "System/Library/Frameworks/CoreLocation.framework", "group": "SDKROOT", "searchName": "corelocation.framework", "strong": "Required"],
                ["name": "QuartzCore.framework", "path": "System/Library/Frameworks/QuartzCore.framework", "group": "SDKROOT", "searchName": "quartzcore.framework", "strong": "Required"],
                ["name": "SystemConfiguration.framework", "path": "System/Library/Frameworks/SystemConfiguration.framework", "group": "SDKROOT", "searchName": "systemconfiguration.framework", "strong": "Weak"],
                ["name": "CoreTelephony.framework", "path": "System/Library/Frameworks/CoreTelephony.framework", "group": "SDKROOT", "searchName": "coretelephony.framework", "strong": "Weak"],
                ["name": "AudioToolbox.framework", "path": "System/Library/Frameworks/AudioToolbox.framework", "group": "SDKROOT", "searchName": "audiotoolbox.framework", "strong": "Required"],
                ["name": "Security.framework", "path": "System/Library/Frameworks/Security.framework", "group": "SDKROOT", "searchName": "security.framework", "strong": "Required"]
        ]

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
            builder << "\t" * level
            node.key.each {
                builder << "\"${it.text()}\" = "
                builder << printElementToString(getNextNode(it), level + 1)
                builder << ";\n"
                builder << "\t" * level
            }
            builder << "}"
        } else if (node.name().equals("array")) {
            // its list or array
            builder << "(\n"
            builder << "\t" * level
            node.string.each {
                builder << printElementToString(it, level + 1)
                builder << ",\n"
                builder << "\t" * level
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
                    array.appendNode("string", "\$(SRCROOT)/Apphance-Pre-Production.framework")
                } else {
                    librarySearchPaths.appendNode("string", "\$(SRCROOT)/Apphance-Pre-Production.framework")
                }

            }
        }
    }


    String addApphanceToProject(File projectRootDirectory, File xcodeProject, String targetName, String configurationName, String appKey) {
        logger.lifecycle("Adding Apphance to target " + targetName + " configuration " + configurationName)
        rootObject = getParsedProject(new File(xcodeProject, PROJECT_PBXPROJ), targetName)
        def project = getObject(getProperty(rootObject.dict, "rootObject").text())
        IOSApphanceSourceHelper sourceHelper = this.apphanceSourceHelper
        getProperty(project, "targets").'*'.each { target ->
            def targetText = target.text()
            if (getProperty(getObject("${targetText}"), "name").text().equals(targetName)) {
                // find build phases in target
                getProperty(getObject("${targetText}"), "buildPhases").'*'.each { phase ->
                    // find frameworks in build phases
                    def phaseText = phase.text()
                    if (getProperty(getObject("${phaseText}"), "isa").text().equals("PBXFrameworksBuildPhase")) {
                        addApphanceToFramework(getObject("${phaseText}"))
                    } else if (getProperty(getObject("${phaseText}"), "isa").text().equals("PBXSourcesBuildPhase")) {
                        replaceLogsWithApphance(projectRootDirectory, getObject("${phaseText}"), project)
                    }
                }
                if (!hasApphance) {
                    // Find pch file with proper configuration
                    def buildConfigurationList = getProperty(getObject("${targetText}"), "buildConfigurationList")
                    getProperty(getObject(buildConfigurationList.text()), "buildConfigurations").'*'.each { configuration ->
                        if (getProperty(getObject(configuration.text()), "name").text().equals(configurationName)) {
                            sourceHelper.addApphanceToPch(new File(projectRootDirectory, getProperty(getProperty(getObject(configuration.text()), "buildSettings"),
                                    "GCC_PREFIX_HEADER").text()))
                        }
                    }
                }
            }
        }
        if (!hasApphance) {
            addFlagsAndPathsToProject(project, configurationName)
            apphanceSourceHelper.addApphanceInit(projectRootDirectory, appKey)
        }

        File f = new File(projectRootDirectory, "newProject.pbxproj")
        f.delete()
        f.withWriter { writer ->
            writer << writePlistToString()
        }
        String[] paths = xcodeProject.canonicalPath.split("/")
        String xcodeProjectFileName = paths[paths.size() - 1]
        projectFile = new File(new File(projectRootDirectory, "${xcodeProjectFileName}"), PROJECT_PBXPROJ)
        logger.lifecycle("Writing file " + projectFile)
        projectFile.delete()
        projectFile.withWriter { out ->
            out << f.text
        }
        f.delete()
        return projectFile.text
    }

    void buildProjectTree(Object group, HashMap<String, String> objects, String actualPath) {
        String path = actualPath
        if (getProperty(group, "path") != null) {
            path = path + getProperty(group, "path").text() + "/"
        }
        boolean found = false
        getProperty(group, "children").each {
            def child = getObject(it.text())
            if (getProperty(child, "isa").text().equals("PBXFileReference")) {
                objects.put(it.text(), path + getProperty(getObject(it.text()), "path").text())
            } else if (getProperty(child, "isa").text().equals("PBXGroup")) {
                buildProjectTree(child, objects, path)
            }
        }
    }

    void replaceLogsWithApphance(File projectRootDir, Object sourcesPhase, Object project) {
        logger.lifecycle("Replacing NSLog logs with Apphance in ${projectRootDir}")
        def files = getProperty(sourcesPhase, "files")
        def mainGroup = getObject(getProperty(project, "mainGroup").text())
        HashMap<String, String> objects = new HashMap<String, String>()
        buildProjectTree(mainGroup, objects, "")
        new AntBuilder().replace(casesensitive: 'true', token: 'NSLog',
                value: 'APHLog', summary: true) {
            fileset(dir: projectRootDir) {
                files.each {
                    def property = getProperty(getObject(it.text()), "fileRef")
                    if (property != null) {
                        include(name: objects[property.text()])
                    }
                }
            }
        }
    }

}
