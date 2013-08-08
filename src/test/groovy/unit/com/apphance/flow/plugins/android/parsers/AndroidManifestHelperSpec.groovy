package com.apphance.flow.plugins.android.parsers

import com.apphance.flow.TestUtils
import groovy.util.slurpersupport.GPathResult
import org.gradle.api.GradleException
import spock.lang.Shared
import spock.lang.Specification

import static android.Manifest.permission.ACCESS_MOCK_LOCATION
import static com.apphance.flow.plugins.android.parsers.AndroidManifestHelper.MAIN_ACTIVITY_FILTER
import static com.google.common.io.Files.copy

@Mixin(TestUtils)
class AndroidManifestHelperSpec extends Specification {

    @Shared
    def basic = new File('testProjects/android/android-basic')
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
        def tmpDir = temporaryDir

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
    }

    def 'package and label is replaced correctly'() {
        given:
        def tmpDir = temporaryDir

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
        def tmpDir = temporaryDir

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
    }

    def 'main activity name is read correctly'() {
        expect:
        androidManifestHelper.getMainActivitiesFromProject(new File('testProjects/apphance-updates/')) == ['pl.morizon.client.ui.HomeActivity'] as Set
    }

    def 'two main activities is read correctly'() {
        expect:
        androidManifestHelper.getMainActivitiesFromProject(new File('src/test/resources/com/apphance/flow/android'), 'ManifestWithTwoMainActivitiesOneInAlias.xml') ==
                ['com.apphance.flowTest.android.ui.SecondMainActivity', 'com.apphance.flowTest.android.TestActivity'] as Set
    }

    def 'alias main activities is read correctly'() {
        expect:
        androidManifestHelper.getMainActivitiesFromProject(new File('src/test/resources/com/apphance/flow/android'), 'ManifestWithTwoMainActivitiesOneInAlias.xml') ==
                ['com.apphance.flowTest.android.ui.SecondMainActivity', 'com.apphance.flowTest.android.TestActivity'] as Set
    }

    def 'exception thrown when no main activity can be found'() {
        when:
        androidManifestHelper.getActivities(new File('src/test/resources/com/apphance/flow/android/AndroidManifestWithoutMainActivity.xml'), MAIN_ACTIVITY_FILTER)

        then:
        def e = thrown(GradleException)
        e.message == 'Main activity could not be found!'
    }

    def 'apphance activity is not found'() {
        expect:
        !androidManifestHelper.isApphanceActivityPresent(basic)
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

    def 'test extractClassName'() {
        expect:
        result == androidManifestHelper.extractClassName(packageName, className)

        where:
        className               | packageName   | result
        'Activity'              | 'com.polidea' | 'com.polidea.Activity'
        '.Activity'             | 'com.polidea' | 'com.polidea.Activity'
        'com.polidea.Activity'  | 'com.polidea' | 'com.polidea.Activity'
        'com.facebook.Activity' | 'com.polidea' | 'com.facebook.Activity'

    }


    def 'test maxLibNumber'() {
        given:
        def lines = """
            |android.library.reference.1=../libs/cocos2d/cocos2dx/platform/android/java
            |android.library.reference.4=libs/facebook-android-sdk/facebook
            |android.library.reference.10=libs/google-play-services_lib""".stripMargin().split('\n')*.trim()

        expect:
        androidManifestHelper.maxLibNumber(lines) == 10
    }

    private GPathResult parsedManifest(File manifest) {
        new XmlSlurper().parse(manifest)
    }
}
