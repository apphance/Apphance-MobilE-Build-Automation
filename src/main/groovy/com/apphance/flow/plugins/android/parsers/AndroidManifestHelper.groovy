package com.apphance.flow.plugins.android.parsers

import com.apphance.flow.util.Preconditions
import groovy.util.slurpersupport.FilteredNodeChildren
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

    private logger = Logging.getLogger(getClass())

    static final String ANDROID_MANIFEST = 'AndroidManifest.xml'

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

        logger.info("Replacing package: '$packageName' with new package: '$newPkg'")
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

    private void addPermissionsToManifest(GPathResult manifest, String... permissions) {
        permissions.each { p ->
            def permission = manifest.'uses-permission'.find { it.@'android:name'.text().equals(p) }

            if (permission == null || permission.isEmpty()) {
                manifest.appendNode({ 'uses-permission'('android:name': p) })
            }
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
        } else if (className.contains('.')) {
            className
        } else {
            "${packageName}.${className}"
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

    boolean isApphanceActivityPresent(File projectDir) {
        def file = new File(projectDir, ANDROID_MANIFEST)
        def manifest = new XmlSlurper().parse(file)

        return manifest.application.activity.find {
            def activityName = it.@'android:name'.text().toLowerCase()
            activityName.equals('com.apphance.android.ui.loginactivity') ||
                    activityName.equals('com.apphance.android.ui.problemactivity')
        }
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

    void addLibrary(File projectProperties, String lib) {
        assert projectProperties.exists()
        List<String> lines = projectProperties.readLines()
        int libSize = maxLibNumber(lines)
        projectProperties << "android.library.reference.${libSize + 1}=$lib"
    }

    int maxLibNumber(List<String> lines) {
        def nums = [0]
        def libRefRegex = /android.library.reference.(\d+).*/
        lines.findAll { (it =~ libRefRegex).matches() }.each {
            def matcher = it =~ libRefRegex
            def num = matcher[0][1]
            try {
                nums += num as Integer
            } catch (NumberFormatException ex) {
                logger.error "Error during etracting lib number from $it"
            }
        }
        nums.max()
    }
}
