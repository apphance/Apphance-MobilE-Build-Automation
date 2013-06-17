package com.apphance.flow.plugins.android.parsers

import groovy.util.slurpersupport.GPathResult
import org.gradle.api.GradleException
import spock.lang.Shared
import spock.lang.Specification

import static android.Manifest.permission.ACCESS_MOCK_LOCATION
import static com.google.common.io.Files.copy
import static com.google.common.io.Files.createTempDir

class AndroidManifestHelperSpec extends Specification {

    @Shared
    def basic = new File('testProjects/android/android-basic')
    @Shared
    def noApphanceApplication = new File('testProjects/android/android-no-apphance-application')
    @Shared
    def androidManifestHelper = new AndroidManifestHelper()

    def 'package is read correctly'() {
        expect:
        androidManifestHelper.androidPackage(basic) == 'com.apphance.flowTest.android'
    }

    def 'version is read correctly'() {
        given:
        def versionDetails = androidManifestHelper.readVersion(basic)

        expect:
        versionDetails.versionString == '1.0.1'
        versionDetails.versionCode == '42'
    }

    def 'project icon is found'() {
        expect:
        'icon' == androidManifestHelper.readIcon(basic)
    }

    def 'version is updated and read correctly'() {
        given:
        def tmpDir = createTempDir()

        and:
        copy(new File(basic, com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST), new File(tmpDir, com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST))

        and:
        def versionString = '123_456_789'
        def versionCode = '3145'

        expect:
        !(androidManifestHelper.readVersion(basic).versionString == versionString)
        !(androidManifestHelper.readVersion(basic).versionCode == versionCode)

        when:
        androidManifestHelper.updateVersion(tmpDir, versionString, versionCode)

        then:
        def versionDetails = androidManifestHelper.readVersion(tmpDir)
        versionDetails.versionString == versionString
        versionDetails.versionCode == versionCode

        cleanup:
        tmpDir.deleteDir()
    }

    def 'package and label is replaced correctly'() {
        given:
        def tmpDir = createTempDir()

        and:
        def tmpManifest = new File(tmpDir, com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST)

        and:
        copy(new File(basic, com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST), tmpManifest)

        expect:
        !(parsedManifest(tmpManifest).@package == newPkg)
        !(getLabel(tmpManifest) == newLbl)

        when:
        androidManifestHelper.replacePackage(tmpDir, 'com.apphance.flowTest.android', newPkg, newLbl)

        then:
        androidManifestHelper.androidPackage(tmpDir) == newPkg
        getLabel(tmpManifest) == expectedLbl

        cleanup:
        tmpDir.deleteDir()

        where:
        newPkg                              | newLbl                 | expectedLbl
        'com.apphance.flowTest.android.new' | null                   | '@string/app_name'
        'com.apphance.flowTest.android.new' | '@string/app_name_new' | '@string/app_name_new'
    }

    private String getLabel(File manifest) {
        parsedManifest(manifest).application.@'android:label'.text()
    }

    def 'exception is thrown on bad package'() {
        when:
        androidManifestHelper.replacePackage(basic, 'sample1', 'sample2')

        then:
        def e = thrown(GradleException)
        e.message == "Package to replace in manifest is: 'com.apphance.flowTest.android' and not expected: 'sample1' (neither target: 'sample2'). This must be wrong."
    }

    def 'permissions are added correctly'() {
        given:
        def tmpDir = createTempDir()

        and:
        def tmpManifest = new File(tmpDir, com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST)

        and:
        copy(new File(basic, com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST), tmpManifest)

        expect:
        !(parsedManifest(tmpManifest).'uses-permission'.@'android:name'.find {
            it.text() == 'android.permission.ACCESS_MOCK_LOCATION'
        })

        when:
        androidManifestHelper.addPermissions(tmpDir, ACCESS_MOCK_LOCATION)

        then:
        parsedManifest(tmpManifest).'uses-permission'.@'android:name'.find {
            it.text() == 'android.permission.ACCESS_MOCK_LOCATION'
        }

        cleanup:
        tmpDir.deleteDir()
    }

    def 'apphance is added correctly'() {
        given:
        def tmpDir = createTempDir()

        and:
        def tmpManifest = new File(tmpDir, com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST)

        and:
        copy(new File(noApphanceApplication, com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST), tmpManifest)

        expect:
        !androidManifestHelper.isApphanceActivityPresent(tmpDir)
        !androidManifestHelper.isApphanceInstrumentationPresent(tmpDir)

        when:
        androidManifestHelper.addApphance(tmpDir)

        then:
        def manifest = parsedManifest(tmpManifest)

        and:
        manifest.instrumentation.@'android:name'.text() == 'com.apphance.android.ApphanceInstrumentation'
        manifest.instrumentation.@targetPackage.text() == 'com.apphance.flowTest.android'

        and:
        manifest.'uses-permission'.@'android:name'*.text().containsAll(
                'android.permission.INTERNET',
                'android.permission.READ_PHONE_STATE',
                'android.permission.GET_TASKS')

        and:
        manifest.application.activity.@'android:name'*.text().containsAll(
                'com.apphance.android.ui.LoginActivity',
                'com.apphance.android.ui.ProblemActivity',
                'com.apphance.android.LauncherActivity')

        and:
        def pa = manifest.application.activity.find { it.@'android:name' == 'com.apphance.android.ui.ProblemActivity' }
        pa.@configChanges.text() == 'orientation'
        pa.@launchMode.text() == 'singleInstance'

        and:
        def la = manifest.application.activity.find { it.@'android:name' == 'com.apphance.android.LauncherActivity' }
        la.@theme == '@android:style/Theme.Translucent.NoTitleBar'

        and:
        def aa = manifest.application.'activity-alias'.find { it.@'andoid:name' == '.ApphanceLauncherActivity' }
        aa.@targetActivity == 'com.apphance.android.LauncherActivity'

        and:
        manifest.application.activity.'intent-filter'.find {
            it.action.@'android:name' == 'com.apphance.android.LAUNCH'
        }.category.@'android:name'.text() == 'android.intent.category.DEFAULT'

        cleanup:
        tmpDir.deleteDir()
    }

    def 'main activity name is read correctly deprecated method'() {
        expect:
        androidManifestHelper.getMainActivityName(new File('testProjects/apphance-updates/')) == 'pl.morizon.client.ui.HomeActivity'
    }

    def 'main activity name is read correctly'() {
        expect:
        androidManifestHelper.getMainActivitiesFromProject(new File('testProjects/apphance-updates/')) == ['pl.morizon.client.ui.HomeActivity']
    }

    def 'two main activities is read correctly'() {
        expect:
        androidManifestHelper.getMainActivitiesFromProject(new File('src/test/resources/com/apphance/flow/android'), 'ManifestWithTwoMainActivitiesOneInAlias.xml') ==
                ['com.apphance.flowTest.android.ui.SecondMainActivity', 'com.apphance.flowTest.android.TestActivity']
    }

    def 'alias main activities is read correctly'() {
        expect:
        androidManifestHelper.getMainActivitiesFromProject(new File('src/test/resources/com/apphance/flow/android'), 'ManifestWithTwoMainActivitiesOneInAlias.xml') ==
                ['com.apphance.flowTest.android.ui.SecondMainActivity', 'com.apphance.flowTest.android.TestActivity']
    }

    def 'exception thrown when no main activity can be found'() {
        when:
        androidManifestHelper.getMainActivities(new File('src/test/resources/com/apphance/flow/android/AndroidManifestWithoutMainActivity.xml'))

        then:
        def e = thrown(GradleException)
        e.message == 'Main activity could not be found!'
    }

    def 'application name is read correctly'() {
        expect:
        androidManifestHelper.getApplicationName(dir) == expectedName

        where:
        dir                   | expectedName
        basic                 | ''
        noApphanceApplication | 'com.apphance.flowTest.android.MainApplication'
    }

    def 'apphance activity is not found'() {
        expect:
        !androidManifestHelper.isApphanceActivityPresent(basic)
    }

    def 'apphance activity is found'() {
        given:
        def tmpDir = createTempDir()

        and:
        def tmpManifest = new File(tmpDir, com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST)

        and:
        copy(new File(noApphanceApplication, com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST), tmpManifest)

        expect:
        !androidManifestHelper.isApphanceActivityPresent(tmpDir)

        when:
        androidManifestHelper.addApphance(tmpDir)

        then:
        androidManifestHelper.isApphanceActivityPresent(tmpDir)

        cleanup:
        tmpDir.deleteDir()
    }

    def 'apphance instrumentation is found'() {
        given:
        def tmpDir = createTempDir()

        and:
        def tmpManifest = new File(tmpDir, com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST)

        and:
        copy(new File(noApphanceApplication, com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.ANDROID_MANIFEST), tmpManifest)

        expect:
        !androidManifestHelper.isApphanceInstrumentationPresent(tmpDir)

        when:
        androidManifestHelper.addApphance(tmpDir)

        then:
        androidManifestHelper.isApphanceInstrumentationPresent(tmpDir)

        cleanup:
        tmpDir.deleteDir()
    }

    def 'test getSourcesOf'() {
        when:
        def files = androidManifestHelper.getSourcesOf(basic, ['com.apphance.flowTest.android.TestActivity', 'com.apphance.flowTest.android.AnotherActivity'])

        then:
        files
        files.size() == 2
        files.every { it.exists() }
        files.collect { it.name } == ['TestActivity.java', 'AnotherActivity.java']
    }

    private GPathResult parsedManifest(File manifest) {
        new XmlSlurper().parse(manifest)
    }
}
