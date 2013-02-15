package com.apphance.ameba.android

import com.apphance.ameba.util.Preconditions
import com.sun.org.apache.xpath.internal.XPathAPI
import groovy.util.slurpersupport.GPathResult
import groovy.xml.XmlUtil
import org.gradle.api.logging.Logging
import org.w3c.dom.Element

import javax.xml.parsers.DocumentBuilderFactory

/**
 * Helps to parse and process android manifest.
 *
 */
@Mixin(Preconditions)
class AndroidManifestHelper {

    def l = Logging.getLogger(AndroidManifestHelper.class)

    private final String ANDROID_MANIFEST = 'AndroidManifest.xml'

    private final String ATTR_ANDROID_NAME = 'android:name'


    Element getParsedManifest(File projectDirectory) {
        def builderFactory = DocumentBuilderFactory.newInstance()
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        builderFactory.setFeature("http://xml.org/sax/features/validation", false)
        def builder = builderFactory.newDocumentBuilder()
        def inputStream = new FileInputStream(new File(projectDirectory, ANDROID_MANIFEST))
        return builder.parse(inputStream).documentElement
    }

    Expando readVersion(File projectDirectory) {
        def manifest = new XmlSlurper(false, true).parse(new File(projectDirectory, ANDROID_MANIFEST))
        def versionCode = manifest.@'android:versionCode'.text().toLong()
        def versionString = manifest.@'andoid:versionName'.text()
        new Expando(versionCode: versionCode, versionString: versionString)
    }

    void updateVersion(File projecDirectory, Expando versionDetails) {
        def file = new File(projecDirectory, ANDROID_MANIFEST)
        saveOriginalFile(projecDirectory, file)
        def manifest = new XmlSlurper(false, false).parse(file)
        manifest.@'android:versionName' = versionDetails.versionString.toString()
        manifest.@'android:versionCode' = versionDetails.versionCode.toString()
        file.delete()
        file << XmlUtil.serialize(manifest)
    }

    void replacePackage(File projectDir, String oldPkg, String newPkg, String newLbl) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        saveOriginalFile(projectDir, file)

        def manifest = new XmlSlurper(false, false).parse(file)
        def packageName = manifest.@package

        throwIf((packageName != oldPkg && packageName != newPkg), "Package to replace in manifest is: '$packageName' and not expected: '$oldPkg' (neither target: '$newPkg'). This must be wrong.")

        l.lifecycle("Replacing package: '$packageName' with new package: '$newPkg'")
        manifest.@package = newPkg

        if (newLbl) {
            replaceAndroidLabel(manifest, newLbl)
        }

        file.delete()
        file << XmlUtil.serialize(manifest)
    }

    private void saveOriginalFile(File projectDirectory, File file) {
        def originalFile = new File(projectDirectory, file.name + ".orig")
        originalFile.delete()
        originalFile << file.text
    }

    private void replaceAndroidLabel(GPathResult manifest, String newLbl) {
        manifest.application.each {
            it.@'android:label' = newLbl
        }
    }

    void removeApphance(File projectDirectory) {
        def file = new File(projectDirectory, ANDROID_MANIFEST)
        def root = getParsedManifest(projectDirectory)
        saveOriginalFile(projectDirectory, file)
        def manifestNode = XPathAPI.selectSingleNode(root, '/manifest')
        if (manifestNode.attributes.getNamedItem('xmlns:apphance') != null) {
            manifestNode.attributes.removeNamedItem('xmlns:apphance')
        } else {
            l.lifecycle("There is no xmlns:apphance namespace defined in manifest. Skipping apphance removal.")
            return
        }
        XPathAPI.selectNodeList(root, '/manifest/application/activity').each { activity ->
            if (activity.attributes.nodes.any { it.name == 'apphance:only' && it.value == 'true' }) {
                activity.ownerNode.removeChild(activity)
            }
        }
        XPathAPI.selectNodeList(root, '/manifest/application/activity-alias').each { activityAlias ->
            if (activityAlias.attributes.nodes.any { it.name == 'apphance:only' && it.value == 'true' }) {
                activityAlias.ownerNode.removeChild(activityAlias)
            }
        }
        XPathAPI.selectNodeList(root, '/manifest/uses-permission').each { uses_permission ->
            if (uses_permission.attributes.nodes.any { it.name == 'apphance:only' && it.value == 'true' }) {
                uses_permission.ownerNode.removeChild(uses_permission)
            }
        }
        XPathAPI.selectNodeList(root, '/manifest/instrumentation').each { instrumentation ->
            if (instrumentation.attributes.nodes.any { it.name == 'apphance:only' && it.value == 'true' }) {
                instrumentation.ownerNode.removeChild(instrumentation)
            }
        }
        XPathAPI.selectNodeList(root, '/manifest/application/activity/intent-filter/action[@name=\'com.apphance.android.LAUNCH\']').each { action ->
            action.attributes.nodes.each {
                if (it.name == ATTR_ANDROID_NAME) {
                    it.value = 'android.intent.action.MAIN'
                }
            }
            action.parentNode.childNodes.each {
                if (it.nodeName == 'category') {
                    it.attributes.nodes.each { attribute ->
                        if (attribute.name == ATTR_ANDROID_NAME) {
                            attribute.value = 'android.intent.category.LAUNCHER'
                        }
                    }
                }
            }
        }
        def fileAgain = new File(projectDirectory, ANDROID_MANIFEST)
        fileAgain.delete()
        fileAgain.write(root as String)
    }

    public void addPermissionsToManifest(File projectDirectory, def permissionsToAdd) {
        def file = new File(projectDirectory, ANDROID_MANIFEST)
        saveOriginalFile(projectDirectory, file)
        XmlSlurper slurper = new XmlSlurper(false, true)
        GPathResult manifest = slurper.parse(file)
        // Add permissions
        def permissions = manifest."uses-permission"
        l.lifecycle(permissions.text())
        permissionsToAdd.each { currentPermission ->
            l.lifecycle("Finding permission " + currentPermission)
            def foundPermission = permissions.find { it.@"$ATTR_ANDROID_NAME".text().equals(currentPermission) }
            if (foundPermission == null || foundPermission.isEmpty()) {
                l.lifecycle("Permission " + currentPermission + " not found")
                manifest.appendNode({ 'uses-permission'("$ATTR_ANDROID_NAME": currentPermission) })
            } else {
                l.lifecycle("Permission " + foundPermission.@"$ATTR_ANDROID_NAME".text() + " found")
            }
        }
        file.delete()
        def outputBuilder = new groovy.xml.StreamingMarkupBuilder()
        outputBuilder.encoding = 'UTF-8'
        outputBuilder.useDoubleQuotes = true

        String result = outputBuilder.bind {
            mkp.xmlDeclaration()
            mkp.yield manifest
        }
        file.withWriter { writer ->
            writer << result.replace(">", ">\n").replace("xmlns:tag0=\"\"", "").replace("tag0:", "")
        }
    }

    public void addApphanceToManifest(File projectDirectory) {
        def file = new File(projectDirectory, ANDROID_MANIFEST)
        saveOriginalFile(projectDirectory, file)
        XmlSlurper slurper = new XmlSlurper(false, true)
        GPathResult manifest = slurper.parse(file)
        String packageName = manifest.@package

        // Add instrumentation
        manifest.appendNode({
            instrumentation("${ATTR_ANDROID_NAME}": "com.apphance.android.ApphanceInstrumentation",
                    'android:targetPackage': packageName)
        })

        // Add permissions
        def permissions = manifest."uses-permission"
        l.lifecycle(permissions.text())
        def apphancePermissions = ["android.permission.INTERNET", "android.permission.READ_PHONE_STATE", "android.permission.GET_TASKS"]
        apphancePermissions.each { apphancePermission ->
            l.lifecycle("Finding permission " + apphancePermission)
            def permission = permissions.find { it.@"${ATTR_ANDROID_NAME}".text().equals(apphancePermission) }
            if (permission == null || permission.isEmpty()) {
                l.lifecycle("Permission " + apphancePermission + " not found")
                manifest.appendNode({ 'uses-permission'("${ATTR_ANDROID_NAME}": apphancePermission) })
            } else {
                l.lifecycle("Permission " + permission.@"${ATTR_ANDROID_NAME}".text() + " found")
            }
        }

        // Add apphance activities

        def apphanceActivities = [{ activity("${ATTR_ANDROID_NAME}": "com.apphance.android.ui.LoginActivity") },
                { activity("${ATTR_ANDROID_NAME}": "com.apphance.android.ui.ProblemActivity", "configChanges": "orientation", "launchMode": "singleInstance") },
                { activity("${ATTR_ANDROID_NAME}": "com.apphance.android.LauncherActivity", "theme": "@android:style/Theme.Translucent.NoTitleBar") }]
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
        l.lifecycle("Main activity name = " + mainActivity)
        manifest.application.activity.each {
            if (it.@"${ATTR_ANDROID_NAME}".text().contains(mainActivity)) {
                it."intent-filter".each { filter ->
                    if (filter.action.size() > 0 && filter.action.@"${ATTR_ANDROID_NAME}".text().equals('android.intent.action.MAIN')) {
                        filter.action.replaceNode({ action("${ATTR_ANDROID_NAME}": "com.apphance.android.LAUNCH") })
                    }
                    if (filter.category.size() > 0 && filter.category.@"${ATTR_ANDROID_NAME}".text().equals('android.intent.category.LAUNCHER')) {
                        filter.category.replaceNode({ category("${ATTR_ANDROID_NAME}": "android.intent.category.DEFAULT") })
                    }
                }
            }
        }

        file.delete()
        def outputBuilder = new groovy.xml.StreamingMarkupBuilder()
        outputBuilder.encoding = 'UTF-8'
        outputBuilder.useDoubleQuotes = true

        String result = outputBuilder.bind {
            mkp.xmlDeclaration()
            mkp.yield manifest
        }
        file.withWriter { writer ->
            writer << result.replace(">", ">\n").replace("xmlns:tag0=\"\"", "").replace("tag0:", "")
        }
    }

    public String getMainActivityName(File projectDirectory) {
        def file = new File(projectDirectory, ANDROID_MANIFEST)
        def manifest = new XmlSlurper(false, true).parse(file)

        def activities = manifest.application.activity

        def mainActivity = activities.find {
            'android.intent.action.MAIN' in it.'intent-filter'.action.@"$ATTR_ANDROID_NAME"*.text() &&
                    'android.intent.category.LAUNCHER' in it.'intent-filter'.category.@"$ATTR_ANDROID_NAME"*.text()
        }

        throwIf(mainActivity.isEmpty(), 'Main activity could not be found!')

        def packageName = manifest.@package.text()
        def className = mainActivity.@"$ATTR_ANDROID_NAME".text()

        packageName = className.startsWith('.') ? packageName : packageName + '.'
        packageName = className.startsWith(packageName) ? '' : packageName

        packageName + className
    }

    public String getApplicationName(File projectDirectory) {
        def file = new File(projectDirectory, ANDROID_MANIFEST)
        def manifest = new XmlSlurper().parse(file)

        String packageName = manifest.@package
        String applicationName = manifest.application.@"$ATTR_ANDROID_NAME".text()

        packageName = applicationName.startsWith('.') ? packageName : packageName + '.'
        packageName = applicationName.contains(packageName) ? '' : packageName

        return packageName + applicationName
    }

    public boolean isApphanceActivityPresent(File projectDirectory) {
        def file = new File(projectDirectory, ANDROID_MANIFEST)
        def manifest = new XmlSlurper().parse(file)

        return manifest.activity.find {
            def activityName = it.@"$ATTR_ANDROID_NAME".text().toLowerCase()
            activityName.equals('com.apphance.android.ui.loginactivity') || activityName.equals('com.apphance.android.ui.problemactivity')
        }.size() != 0
    }

    public boolean isApphanceInstrumentationPresent(File projectDirectory) {
        def file = new File(projectDirectory, ANDROID_MANIFEST)
        def manifest = new XmlSlurper().parse(file)
        return manifest.instrumentation.find { it.@"$ATTR_ANDROID_NAME".text().toLowerCase().equals('com.apphance.android.apphanceinstrumentation') }.size() != 0
    }

    void restoreOriginalManifest(File projectDirectory) {
        def file = new File(projectDirectory, ANDROID_MANIFEST)
        def originalFile = new File(projectDirectory, file.name + '.orig')
        if (originalFile.exists()) {
            file.delete()
            file << originalFile.text
            originalFile.delete()
        } else {
            l.warn("Could not restore original file. It's missing!")
        }
    }
}