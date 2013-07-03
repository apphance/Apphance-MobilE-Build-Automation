package com.apphance.flow.plugins.android.parsers

import com.apphance.flow.util.Preconditions
import groovy.util.slurpersupport.FilteredNodeChildren
import groovy.util.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import org.gradle.api.logging.Logging

import static android.Manifest.permission.GET_TASKS
import static android.Manifest.permission.INTERNET
import static android.Manifest.permission.READ_PHONE_STATE

/**
 * Helps to parse and process android manifest.
 *
 */
@Mixin(Preconditions)
class AndroidManifestHelper {

    private def logger = Logging.getLogger(getClass())

    static final String ANDROID_MANIFEST = 'AndroidManifest.xml'

    static final String APPHANCE_ALIAS =
        '''
<activity-alias android:name=".ApphanceLauncherActivity" android:targetActivity="com.apphance.android.LauncherActivity">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity-alias>
'''

    String androidPackage(File projectDir) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        def manifest = new XmlSlurper().parse(file)
        manifest.@package
    }

    Map<String, String> readVersion(File projectDir) {
        def manifest = new XmlSlurper().parse(new File(projectDir, ANDROID_MANIFEST))
        String versionCode = manifest.@'android:versionCode'.text()
        String versionString = manifest.@'android:versionName'.text()
        [versionCode: versionCode, versionString: versionString]
    }

    String readIcon(File projectDir) {
        def manifest = new XmlSlurper().parse(new File(projectDir, ANDROID_MANIFEST))
        def icon = manifest.application.'@android:icon'?.text()
        icon ? icon.substring(icon.lastIndexOf('/') + 1, icon.length()) : ''
    }

    void updateVersion(File projectDir, String versionString, String versionCode) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        saveOriginalFile(projectDir, file)
        def manifest = new XmlSlurper(false, false).parse(file)
        manifest.@'android:versionName' = versionString
        manifest.@'android:versionCode' = versionCode
        file.delete()
        file << XmlUtil.serialize(manifest)
    }

    void replacePackage(File projectDir, String oldPkg, String newPkg, String newLbl = null, String newName = null) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        saveOriginalFile(projectDir, file)

        def manifest = new XmlSlurper(false, false).parse(file)
        def packageName = manifest.@package

        throwIfConditionTrue((packageName != oldPkg && packageName != newPkg), "Package to replace in manifest is: " +
                "'$packageName' and not expected: '$oldPkg' (neither target: '$newPkg'). This must be wrong.")

        logger.lifecycle("Replacing package: '$packageName' with new package: '$newPkg'")
        manifest.@package = newPkg

        if (newLbl) {
            replaceAndroidProperty(manifest, 'label', newLbl)
        }

        if (newName) {
            replaceAndroidProperty(manifest, 'name', newName)
        }

        file.delete()
        file << XmlUtil.serialize(manifest)
    }

    private void replaceAndroidProperty(GPathResult manifest, String propertyName, String newValue) {
        manifest.application.each {
            it.@"android:$propertyName" = newValue
        }
    }

    void addPermissions(File projectDir, String... permissions) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        saveOriginalFile(projectDir, file)

        def manifest = new XmlSlurper().parse(file)
        addPermissionsToManifest(manifest, permissions)

        String result = xmlToString(manifest)
        file.delete()
        file.write(replaceTag0(result))
    }

    void addApphance(File projectDir) {
        def f = new File(projectDir, ANDROID_MANIFEST)
        saveOriginalFile(projectDir, f)

        def manifest = new XmlSlurper().parse(f)

        addInstrumentation(manifest)
        addPermissionsToManifest(manifest, INTERNET, READ_PHONE_STATE, GET_TASKS)
        addActivities(manifest)
        addAlias(manifest)

        def mainActivityActions = findMainActivityActions(manifest, projectDir)
        replaceAction(mainActivityActions)
        replaceCategory(mainActivityActions)

        String result = xmlToString(manifest)
        f.delete()
        f.write(replaceTag0(result))
    }

    void withManifest(File projectDir, Closure doSomething) {
        def manifestFile = new File(projectDir, ANDROID_MANIFEST)
        saveOriginalFile(projectDir, manifestFile)

        GPathResult manifest = new XmlSlurper().parse(manifestFile)

        doSomething(manifest)

        String result = xmlToString(manifest)
        manifestFile.delete()
        manifestFile.write(replaceTag0(result))
    }

    private void saveOriginalFile(File projectDir, File file) {
        def originalFile = new File(projectDir, "${file.name}.orig")
        originalFile.delete()
        originalFile << file.text
    }

    private void addInstrumentation(GPathResult manifest) {
        manifest.appendNode({
            instrumentation(
                    'android:name': 'com.apphance.android.ApphanceInstrumentation',
                    'android:targetPackage': manifest.@package)
        })
    }

    private void addPermissionsToManifest(GPathResult manifest, String... permissions) {
        permissions.each { p ->
            def permission = manifest.'uses-permission'.find { it.@'android:name'.text().equals(p) }

            if (permission == null || permission.isEmpty()) {
                manifest.appendNode({ 'uses-permission'('android:name': p) })
            }
        }
    }

    private void addActivities(GPathResult manifest) {
        [
                { activity('android:name': 'com.apphance.android.ui.LoginActivity') },
                {
                    activity('android:name': 'com.apphance.android.ui.ProblemActivity',
                            'configChanges': 'orientation', 'launchMode': 'singleInstance')
                },
                {
                    activity('android:name': 'com.apphance.android.LauncherActivity',
                            'theme': '@android:style/Theme.Translucent.NoTitleBar')
                }
        ].each {
            manifest.application.appendNode(it)
        }
    }

    private void addAlias(GPathResult manifest) {
        def apphanceAlias = new XmlSlurper(false, false).parseText(APPHANCE_ALIAS)
        manifest.application.appendNode(apphanceAlias)
    }

    private findMainActivityActions(GPathResult manifest, File projectDir) {
        String mainActivity = getMainActivityName(projectDir)
        String mainActivityClass = mainActivity.split('\\.').last()
        manifest.application.activity.findAll {
            it.@'android:name'.text().contains(mainActivityClass)
        }
    }

    @Deprecated
    // TODO remove this method and replace invocations with getMainActivities
    String getMainActivityName(File projectDir) {
        getMainActivitiesFromProject(projectDir).find()
    }

    Collection<String> getMainActivitiesFromProject(File projectDir, String manifestName = ANDROID_MANIFEST) {
        def manifestFile = new File(projectDir, manifestName)
        getActivities(manifestFile, MAIN_ACTIVITY_FILTER)
    }

    File getManifest(File projectDir, String manifestName = ANDROID_MANIFEST) {
        new File(projectDir, manifestName)
    }

    public static Closure<Boolean> MAIN_ACTIVITY_FILTER = {
        'android.intent.action.MAIN' in it.'intent-filter'.action.@'android:name'*.text() &&
                'android.intent.category.LAUNCHER' in it.'intent-filter'.category.@'android:name'*.text()
    }

    Set<String> getActivities(File manifestFile, Closure<Boolean> filter = { true }) {
        def manifest = new XmlSlurper().parse(manifestFile)

        def activities = manifest.application.activity
        def activityAliases = manifest.application.'activity-alias'

        FilteredNodeChildren mainActivities = activities.findAll(filter)
        FilteredNodeChildren mainAliasActivities = activityAliases.findAll(filter)

        logger.info("Found activities: ${mainActivities.size()}, alias activities: ${mainAliasActivities.size()}")

        throwIfConditionTrue(!(mainActivities.size() + mainAliasActivities.size()), 'Main activity could not be found!')

        def allActivities = mainActivities.collect { nodeToClassName(manifest, it) } + mainAliasActivities.collect { nodeToClassName(manifest, it) }
        logger.info("All activities from manifest: $allActivities")

        List<String> projectActivities = allActivities.findAll { it.startsWith(manifest.@package.text()) }
        logger.info("Project activities : $projectActivities")
        projectActivities as Set
    }

    String nodeToClassName(GPathResult manifest, GPathResult mainActivity) {
        assert mainActivity.name() in ['activity', 'activity-alias']

        def packageName = manifest.@package.text()
        def className = mainActivity.name() == 'activity' ? mainActivity.@'android:name'.text() : mainActivity.@'android:targetActivity'.text()

        extractClassName(packageName, className)
    }

    String extractClassName(String packageName, String className) {
        if (className.startsWith('.')) {
            packageName + className
        } else if (className.contains('.')){
            className
        } else {
            "${packageName}.${className}"
        }
    }

    private void replaceAction(def activities) {
        activities.findAll {
            def a = it.'intent-filter'.action
            a.size() > 0 && a.@'android:name'.text().equals('android.intent.action.MAIN')
        }.each {
            it.'intent-filter'.action.replaceNode({ action('android:name': 'com.apphance.android.LAUNCH') })
        }
    }

    private void replaceCategory(def activities) {
        activities.findAll {
            def c = it.'intent-filter'.category
            c.size() > 0 && c.@'android:name'.text().equals('android.intent.category.LAUNCHER')
        }.each {
            it.'intent-filter'.category.replaceNode({ category('android:name': 'android.intent.category.DEFAULT') })
        }
    }

    private String xmlToString(GPathResult manifest) {
        def smb = new StreamingMarkupBuilder()
        smb.encoding = 'UTF-8'
        smb.useDoubleQuotes = true

        String result = smb.bind {
            mkp.xmlDeclaration()
            mkp.yield manifest
        }
        result
    }

    //to be removed when gradle is switched to use groovy > 2.0
    private String replaceTag0(String result) {
        result.replace('>', '>\n').replace('xmlns:tag0=""', '').replace('tag0:', '')
    }

    String getApplicationName(File projectDir) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        def manifest = new XmlSlurper().parse(file)

        String packageName = manifest.@package.text()
        String applicationName = manifest.application.@'android:name'.text()

        if (!(packageName?.trim() && applicationName?.trim()))
            return ''

        packageName = applicationName.startsWith('.') ? packageName : packageName + '.'
        packageName = applicationName.contains(packageName) ? '' : packageName

        packageName + applicationName
    }

    boolean isApphanceActivityPresent(File projectDir) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        def manifest = new XmlSlurper().parse(file)

        return manifest.application.activity.find {
            def activityName = it.@'android:name'.text().toLowerCase()
            activityName.equals('com.apphance.android.ui.loginactivity') ||
                    activityName.equals('com.apphance.android.ui.problemactivity')
        }
    }

    boolean isApphanceInstrumentationPresent(File projectDir) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        def manifest = new XmlSlurper().parse(file)

        return manifest.instrumentation.find {
            it.@'android:name'.text().toLowerCase().equals('com.apphance.android.apphanceinstrumentation')
        }.size() != 0
    }

    void restoreOriginalManifest(File projectDir) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        def originalFile = new File(projectDir, "${file.name}.orig")
        if (!originalFile.exists()) {
            logger.warn("Could not restore original file. It's missing!")
            return
        }
        file.delete()
        file << originalFile.text
        originalFile.delete()
    }

    List<File> getSourcesOf(File projDir, Collection<String> classes) {
        classes.collect { new File(projDir, "src/${it.replace('.', '/')}.java") }
    }
}
