package com.apphance.ameba.android

import groovy.util.slurpersupport.GPathResult

import java.io.File

import javax.xml.parsers.DocumentBuilderFactory

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.apphance.ApphanceProperty;
import com.sun.org.apache.xpath.internal.XPathAPI

/**
 * Helps to parse and process android manifest.
 *
 */
class AndroidManifestHelper {

    static Logger logger = Logging.getLogger(AndroidManifestHelper.class)

    org.w3c.dom.Element getParsedManifest(File projectDirectory) {
        def builderFactory = DocumentBuilderFactory.newInstance()
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        builderFactory.setFeature("http://xml.org/sax/features/validation", false)
        def builder = builderFactory.newDocumentBuilder()
        def inputStream = new FileInputStream(new File(projectDirectory,"AndroidManifest.xml"))
        return builder.parse(inputStream).documentElement
    }


    String readPackage(File projectDirectory) {
        def root = getParsedManifest(projectDirectory)
        def appPackage = null
        XPathAPI.selectNodeList(root,'/manifest').each{ apppackage ->
            apppackage.attributes.nodes.each { attribute ->
                if (attribute.name == 'package') {
                    appPackage = attribute.value
                }
            }
        }
        return appPackage
    }


    String readMinSdkVersion(File projectDirectory) {
        def root = getParsedManifest(projectDirectory)
        def minSdkVersion = null
        XPathAPI.selectNodeList(root,'/manifest/uses-sdk').each{ usessdk ->
            usessdk.attributes.nodes.each { attribute ->
                if (attribute.name == 'android:minSdkVersion') {
                    minSdkVersion = attribute.value
                }
            }
        }
        return minSdkVersion
    }


    void readVersion(File projectDirectory, ProjectConfiguration conf) {
        def root = getParsedManifest(projectDirectory)
        XPathAPI.selectNodeList(root,'/manifest').each{ manifest ->
            manifest.attributes.nodes.each { attribute ->
                if (attribute.name == 'android:versionCode') {
                    def versionCodeString = attribute.value
                    try {
                        conf.versionCode = versionCodeString.toLong()
                    } catch (NumberFormatException e) {
                        logger.lifecycle("Format of the ${versionCodeString} is not numeric. Starting from 1.")
                        conf.versionCode = 0
                    }
                }
                if (attribute.name == 'android:versionName') {
                    conf.versionString = attribute.value
                }
            }
        }
    }
    void updateVersion(File projectDirectory, ProjectConfiguration conf) {
        println("${projectDirectory}")
        def file = new File(projectDirectory,"AndroidManifest.xml")
        saveOriginalFile(projectDirectory, file)
        def root = getParsedManifest(projectDirectory)
        XPathAPI.selectNodeList(root,'/manifest').each{ manifest ->
            manifest.attributes.nodes.each { attribute ->
                if (attribute.name == 'android:versionCode') {
                    conf.versionCode += 1
                    attribute.value = conf.versionCode
                }
                if (attribute.name == 'android:versionName') {
                    attribute.value = conf.versionString
                }
            }
        }
        file.delete()
        file.write(root as String)
    }

    void replacePackage(File projectDirectory, ProjectConfiguration conf, String oldPackage, String newPackage, String newLabel) {
        def file = new File(projectDirectory,"AndroidManifest.xml")
        saveOriginalFile(projectDirectory, file)
        def root = getParsedManifest(projectDirectory)
        XPathAPI.selectNodeList(root,'/manifest').each{ manifest ->
            manifest.attributes.nodes.each { attribute ->
                if (attribute.name == 'package') {
                    if (attribute.value == oldPackage) {
                        attribute.value = newPackage
                        logger.lifecycle("Replacing old package ${oldPackage} with new package ${newPackage} ")
                    } else if (attribute.value == newPackage) {
                        logger.lifecycle("NOT Replacing old package ${oldPackage} with new package ${newPackage} as it is already ${newPackage}")
                    } else {
                        throw new GradleException("Package to replace in manifest is ${attribute.value} and not expected ${oldPackage} (neither target ${newPackage}. This must be wrong.")
                    }
                }
            }
        }
        if (newLabel != null) {
            XPathAPI.selectNodeList(root,'/manifest/application').each{ application ->
                application.attributes.nodes.each { attribute ->
                    if (attribute.name == 'android:label') {
                        attribute.value = newLabel
                    }
                }
            }
        }
        file.delete()
        file.write(root as String)
    }

    private saveOriginalFile(File projectDirectory, File file) {
        def originalFile = new File(projectDirectory,file.name + ".orig")
        if (!originalFile.exists()) {
            originalFile << file.text
        }
    }


    void removeApphance(File projectDirectory) {
        def file = new File(projectDirectory,"AndroidManifest.xml")
        def root = getParsedManifest(projectDirectory)
        saveOriginalFile(projectDirectory, file)
        def manifestNode = XPathAPI.selectSingleNode(root, '/manifest')
        if (manifestNode.attributes.getNamedItem('xmlns:apphance') != null) {
            manifestNode.attributes.removeNamedItem('xmlns:apphance')
        } else {
            logger.lifecycle("There is no xmlns:apphance namespace defined in manifest. Skipping apphance removal.")
            return
        }
        XPathAPI.selectNodeList(root,'/manifest/application/activity').each{ activity ->
            if (activity.attributes.nodes.any { it.name == 'apphance:only' && it.value == 'true' }) {
                activity.ownerNode.removeChild(activity)
            }
        }
        XPathAPI.selectNodeList(root,'/manifest/application/activity-alias').each{ activityAlias ->
            if (activityAlias.attributes.nodes.any { it.name == 'apphance:only' && it.value == 'true' }) {
                activityAlias.ownerNode.removeChild(activityAlias)
            }
        }
        XPathAPI.selectNodeList(root,'/manifest/uses-permission').each{ uses_permission ->
            if (uses_permission.attributes.nodes.any { it.name == 'apphance:only' && it.value == 'true' }) {
                uses_permission.ownerNode.removeChild(uses_permission)
            }
        }
        XPathAPI.selectNodeList(root,'/manifest/instrumentation').each{ instrumentation ->
            if (instrumentation.attributes.nodes.any { it.name == 'apphance:only' && it.value == 'true' }) {
                instrumentation.ownerNode.removeChild(instrumentation)
            }
        }
        XPathAPI.selectNodeList(root,'/manifest/application/activity/intent-filter/action[@name=\'com.apphance.android.LAUNCH\']').each{ action ->
            action.attributes.nodes.each {
                if (it.name == 'android:name') {
                    it.value = 'android.intent.action.MAIN'
                }
            }
            action.parentNode.childNodes.each {
                if (it.nodeName == 'category') {
                    it.attributes.nodes.each { attribute ->
                        if (attribute.name == 'android:name') {
                            attribute.value = 'android.intent.category.LAUNCHER'
                        }
                    }
                }
            }
        }
        def fileAgain = new File(projectDirectory,"AndroidManifest.xml")
        fileAgain.delete()
        fileAgain.write(root as String)
    }

    public void addPermissionsToManifest(File projectDirectory, def permissionsToAdd) {
        def file = new File(projectDirectory,"AndroidManifest.xml")
        saveOriginalFile(projectDirectory, file)
        XmlSlurper slurper = new XmlSlurper(false, true)
        GPathResult manifest = slurper.parse(file)
        String androidName = "android:name"
        String packageName = manifest.@package

        // Add permissions
        def permissions = manifest."uses-permission"
        logger.lifecycle(permissions.text())
        permissionsToAdd.each { currentPermission ->
            logger.lifecycle("Finding permission " + currentPermission)
            def foundPermission = permissions.find { it.@"${androidName}".text().equals(currentPermission)}
            if (foundPermission == null || foundPermission.isEmpty()) {
                logger.lifecycle("Permission " + currentPermission + " not found")
                manifest.appendNode({'uses-permission'("${androidName}":currentPermission)})
            } else {
                logger.lifecycle("Permission " + foundPermission.@"${androidName}".text() + " found")
            }
        }
        file.delete()
        def outputBuilder = new groovy.xml.StreamingMarkupBuilder()
        outputBuilder.encoding = 'UTF-8'
        outputBuilder.useDoubleQuotes = true

        String result = outputBuilder.bind{
            mkp.xmlDeclaration()
            mkp.yield manifest
        }
        file.withWriter { writer ->
            writer << result.replace(">", ">\n").replace("xmlns:tag0=\"\"", "").replace("tag0:", "")
        }
    }

    public void addApphanceToManifest(File projectDirectory) {
        def file = new File(projectDirectory,"AndroidManifest.xml")
        saveOriginalFile(projectDirectory, file)
        XmlSlurper slurper = new XmlSlurper(false, true)
        GPathResult manifest = slurper.parse(file)
        String androidName = "android:name"
        String packageName = manifest.@package

        // Add instrumentation
        manifest.appendNode({instrumentation("${androidName}":"com.apphance.android.ApphanceInstrumentation",
            'android:targetPackage':packageName)})

        // Add permissions
        def permissions = manifest."uses-permission"
        logger.lifecycle(permissions.text())
        def apphancePermissions = ["android.permission.INTERNET", "android.permission.READ_PHONE_STATE", "android.permission.GET_TASKS"]
        apphancePermissions.each { apphancePermission ->
            logger.lifecycle("Finding permission " + apphancePermission)
            def permission = permissions.find { it.@"${androidName}".text().equals(apphancePermission)}
            if (permission == null || permission.isEmpty()) {
                logger.lifecycle("Permission " + apphancePermission + " not found")
                manifest.appendNode({'uses-permission'("${androidName}":apphancePermission)})
            } else {
                logger.lifecycle("Permission " + permission.@"${androidName}".text() + " found")
            }
        }

        // Add apphance activities

        def apphanceActivities = [{activity("${androidName}":"com.apphance.android.ui.LoginActivity")},
            {activity("${androidName}":"com.apphance.android.ui.ProblemActivity", "configChanges":"orientation", "launchMode":"singleInstance")},
            {activity("${androidName}":"com.apphance.android.LauncherActivity", "theme":"@android:style/Theme.Translucent.NoTitleBar")}]
        apphanceActivities.each {
            manifest.application.appendNode(it)
        }

        // Add alias
        String apphanceAliasString = '''<activity-alias android:name=\".ApphanceLauncherActivity\" android:targetActivity=\"com.apphance.android.LauncherActivity\">
<intent-filter>
<action android:name=\"android.intent.action.MAIN\" />
<category android:name=\"android.intent.category.LAUNCHER\" />
</intent-filter>
</activity-alias>'''
        def apphanceAlias = new XmlSlurper(false, false).parseText(apphanceAliasString)
        manifest.application.appendNode(apphanceAlias)

        // Replace intent filter in main activity
        String mainActivity = getMainActivityName(projectDirectory)
        def packages = mainActivity.split('\\.')
        mainActivity = packages.last()
        logger.lifecycle("Main activity name = " + mainActivity)
        manifest.application.activity.each {
            if (it.@"${androidName}".text().contains(mainActivity)) {
                it."intent-filter".each { filter ->
                    if (filter.action.size() > 0 && filter.action.@"${androidName}".text().equals('android.intent.action.MAIN')) {
                        filter.action.replaceNode({action("${androidName}":"com.apphance.android.LAUNCH")})
                    }
                    if (filter.category.size() > 0 && filter.category.@"${androidName}".text().equals('android.intent.category.LAUNCHER')) {
                        filter.category.replaceNode({category("${androidName}":"android.intent.category.DEFAULT")})
                    }
                }
            }
        }

        file.delete()
        def outputBuilder = new groovy.xml.StreamingMarkupBuilder()
        outputBuilder.encoding = 'UTF-8'
        outputBuilder.useDoubleQuotes = true

        String result = outputBuilder.bind{
            mkp.xmlDeclaration()
            mkp.yield manifest
        }
        file.withWriter { writer ->
            writer << result.replace(">", ">\n").replace("xmlns:tag0=\"\"", "").replace("tag0:", "")
        }
    }


    public String getMainActivityName(File projectDirectory) {
        def file = new File(projectDirectory,"AndroidManifest.xml")

        def manifest = new XmlSlurper(false, true).parse(file)

        String className = manifest.@package
        String intentFilter = "intent-filter"
        String androidName = "android:name"

        def mainActivity = manifest.application.activity.findAll {
            def intentFilters = []
            def mainIntentFound = false
            def launcherIntentFound = false
            // collect all filter nodes
            it."${intentFilter}".each {
                intentFilters << it
            }
            // iterate through each intent filter node and find proper action and category
            intentFilters.each { intentFilterNode ->
                intentFilterNode.action.each {
                    if (it.@"${androidName}".text().equals('android.intent.action.MAIN')) {
                        mainIntentFound = true
                        return
                    }
                }
                intentFilterNode.category.each {
                    if (it.@"${androidName}".text().equals('android.intent.category.LAUNCHER')) {
                        launcherIntentFound = true
                        return
                    }
                }
            }
            // return value
            mainIntentFound && launcherIntentFound
        }
        if (mainActivity.size() > 0) {
            if (!mainActivity[0].@"${androidName}".text().startsWith(".")) {
                className = className + "."
            }
            if (mainActivity[0].@"${androidName}".text().startsWith(manifest.@package.text())) {
                className = ""
            }
            className = className + mainActivity[0].@"${androidName}".text()
        } else {
            throw new GradleException("Main activity file could not be found")
        }

        return className
    }

    public String getApplicationName(File projectDirectory) {
        def file = new File(projectDirectory,"AndroidManifest.xml")

        def manifest = new XmlSlurper().parse(file)
        String className = manifest.@package

        String androidName = "android:name"
        String applicationName = manifest.application.@"${androidName}".text()
        if (!applicationName.startsWith(".")) {
            className = className + "."
        }
        if (applicationName.contains(manifest.@package.text())) {
            className = ""
        }
        className = className + applicationName
        logger.lifecycle("Searching for application class file " + className)
        return className
    }

    public boolean isApphanceActivityPresent(File projectDirectory) {
        def activityFound = false

        def file = new File(projectDirectory,"AndroidManifest.xml")
        def manifest = new XmlSlurper().parse(file)

        manifest.activity.each {
            def activityName = it.@"android:name".text().toLowerCase()
            if (activityName.equals("com.apphance.android.ui.loginactivity") || activityName.equals("com.apphance.android.ui.ProblemActivity")) {
                activityFound = true
            }
        }

        return activityFound
    }

    public boolean isApphanceInstrumentationPresent(File projectDirectory) {
        def instrumentationFound = false

        def file = new File(projectDirectory,"AndroidManifest.xml")
        def manifest = new XmlSlurper().parse(file)

        manifest.instrumentation.each {
            if (it.@"android:name".text().toLowerCase().equals("com.apphance.android.apphanceinstrumentation")) {
                instrumentationFound = true
            }
        }

        return instrumentationFound
    }

    void restoreOriginalManifest(File projectDirectory) {
        def file = new File(projectDirectory,"AndroidManifest.xml")
        def originalFile = new File(projectDirectory,file.name + ".orig")
        if (originalFile.exists()) {
            file.delete()
            file << originalFile.text
            originalFile.delete()
        } else {
            logger.warn("Could not restore original file. It's missing!")
        }
    }

}
