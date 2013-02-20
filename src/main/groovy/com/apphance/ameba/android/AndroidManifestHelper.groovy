package com.apphance.ameba.android

import com.apphance.ameba.util.Preconditions
import groovy.util.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import org.gradle.api.logging.Logging

/**
 * Helps to parse and process android manifest.
 *
 */
@Mixin(Preconditions)
class AndroidManifestHelper {

    def l = Logging.getLogger(AndroidManifestHelper.class)

    public static final String ANDROID_MANIFEST = 'AndroidManifest.xml'

    private final String APPHANCE_ALIAS =
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

    Expando readVersion(File projectDir) {
        def manifest = new XmlSlurper(false, true).parse(new File(projectDir, ANDROID_MANIFEST))
        def versionCode = manifest.@'android:versionCode'.text().toLong()
        def versionString = manifest.@'andoid:versionName'.text()
        new Expando(versionCode: versionCode, versionString: versionString)
    }

    void updateVersion(File projectDir, Expando versionDetails) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        saveOriginalFile(projectDir, file)
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

        throwIf((packageName != oldPkg && packageName != newPkg), "Package to replace in manifest is: " +
                "'$packageName' and not expected: '$oldPkg' (neither target: '$newPkg'). This must be wrong.")

        l.lifecycle("Replacing package: '$packageName' with new package: '$newPkg'")
        manifest.@package = newPkg

        if (newLbl) {
            replaceAndroidLabel(manifest, newLbl)
        }

        file.delete()
        file << XmlUtil.serialize(manifest)
    }

    private void replaceAndroidLabel(GPathResult manifest, String newLbl) {
        manifest.application.each {
            it.@'android:label' = newLbl
        }
    }

    void removeApphance(File projectDir) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        saveOriginalFile(projectDir, file)

        def manifest = new XmlSlurper().parse(file).declareNamespace(apphance: 'http://apphance.com/android')

        def apphanceNamespace = manifest.lookupNamespace('apphance')
        if (!apphanceNamespace) {
            l.lifecycle("There is no xmlns:apphance namespace defined in manifest. Skipping apphance removal.")
            return
        }

        def findClosure = { it.@'apphance:only'.text().toBoolean() }
        def removeClosure = { it.replaceNode {} }

        manifest.application.activity.findAll(findClosure).each(removeClosure)
        manifest.application.'activity-alias'.findAll(findClosure).each(removeClosure)
        manifest.'uses-permission'.findAll(findClosure).each(removeClosure)
        manifest.instrumentation.findAll(findClosure).each(removeClosure)

        def intent = manifest.application.activity.'intent-filter'.find {
            'com.apphance.android.LAUNCH' in it.action.@'android:name'*.text()
        }
        if (!intent.isEmpty()) {
            intent.action.@'name' = 'android.intent.action.MAIN'
            intent.category.@'name' = 'android.intent.category.LAUNCHER'
        }

        String result = xmlToString(manifest)
        file.delete()
        file.write(replaceTag0(result))
    }

    void addPermissionsToManifest(File projectDir, def permissions) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        saveOriginalFile(projectDir, file)

        def manifest = new XmlSlurper(false, true).parse(file)
        addPermissions(manifest, permissions)

        String result = xmlToString(manifest)
        file.delete()
        file.write(replaceTag0(result))
    }

    void addApphanceToManifest(File projectDir) {
        def f = new File(projectDir, ANDROID_MANIFEST)
        saveOriginalFile(projectDir, f)

        def manifest = new XmlSlurper(false, true).parse(f)

        addIntrumentation(manifest)
        addPermissions(manifest,
                'android.permission.INTERNET', 'android.permission.READ_PHONE_STATE', 'android.permission.GET_TASKS')
        addActivities(manifest)
        addAlias(manifest)

        def mainActivityActions = findMainActivityActions(manifest, projectDir)
        replaceAction(mainActivityActions)
        replaceCategory(mainActivityActions)

        String result = xmlToString(manifest)
        f.delete()
        f.write(replaceTag0(result))
    }

    private void saveOriginalFile(File projectDir, File file) {
        def originalFile = new File(projectDir, file.name + ".orig")
        originalFile.delete()
        originalFile << file.text
    }

    private void addIntrumentation(GPathResult manifest) {
        manifest.appendNode({
            instrumentation(
                    'android:name': 'com.apphance.android.ApphanceInstrumentation',
                    'android:targetPackage': manifest.@package)
        })
    }

    private void addPermissions(GPathResult manifest, String... permissions) {
        permissions.each { p ->
            def permission = manifest.'uses-permission'.find { it.@"${'android:name'}".text().equals(p) }

            if (permission == null || permission.isEmpty()) {
                manifest.appendNode({ 'uses-permission'("${'android:name'}": p) })
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
            it.@"${'android:name'}".text().contains(mainActivityClass)
        }
    }

    String getMainActivityName(File projectDir) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        def manifest = new XmlSlurper(false, true).parse(file)

        def activities = manifest.application.activity

        def mainActivity = activities.find {
            'android.intent.action.MAIN' in it.'intent-filter'.action.@'android:name'*.text() &&
                    'android.intent.category.LAUNCHER' in it.'intent-filter'.category.@'android:name'*.text()
        }

        throwIf(mainActivity.isEmpty(), 'Main activity could not be found!')

        def packageName = manifest.@package.text()
        def className = mainActivity.@'android:name'.text()

        packageName = className.startsWith('.') ? packageName : packageName + '.'
        packageName = className.startsWith(packageName) ? '' : packageName

        packageName + className
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

        String packageName = manifest.@package
        String applicationName = manifest.application.@'android:name'.text()

        packageName = applicationName.startsWith('.') ? packageName : packageName + '.'
        packageName = applicationName.contains(packageName) ? '' : packageName

        packageName + applicationName
    }

    boolean isApphanceActivityPresent(File projectDir) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        def manifest = new XmlSlurper().parse(file)

        return manifest.activity.find {
            def activityName = it.@'android:name'.text().toLowerCase()
            activityName.equals('com.apphance.android.ui.loginactivity') ||
                    activityName.equals('com.apphance.android.ui.problemactivity')
        }.size() != 0
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
            l.warn("Could not restore original file. It's missing!")
            return
        }
        file.delete()
        file << originalFile.text
        originalFile.delete()
    }
}
