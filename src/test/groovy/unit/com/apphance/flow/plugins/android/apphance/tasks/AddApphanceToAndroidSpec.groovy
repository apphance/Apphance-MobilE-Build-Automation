package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.util.FlowUtils
import com.google.common.io.Files
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.io.FileUtils
import spock.lang.Specification

import static android.Manifest.permission.*
import static org.apache.commons.io.FileUtils.copyFile

@Mixin([FlowUtils, TestUtils])
class AddApphanceToAndroidSpec extends Specification {

    public static final File TEST_ACTIVITY = new File('src/test/resources/com/apphance/flow/android/TestActivity.java')
    public static final String APPHANCE_IMPORT = 'import com.apphance.android.Apphance;'
    public static final String APPHANCE_LOG_IMPORT = 'import com.apphance.android.Log;'
    def androidVariantConf = GroovySpy(AndroidVariantConfiguration, constructorArgs: ['test variant'])
    def variantDir = Files.createTempDir()
    AddApphanceToAndroid addApphanceToAndroid

    def setup() {
        variantDir.deleteOnExit()
        FileUtils.copyDirectory(new File('testProjects/android/android-basic'), variantDir)

        androidVariantConf.apphanceMode.value = ApphanceMode.QA
        androidVariantConf.apphanceAppKey.value = 'TestKey'
        androidVariantConf.apphanceLibVersion.value = '1.9-RC1'
        androidVariantConf.getTmpDir() >> variantDir

        addApphanceToAndroid = new AddApphanceToAndroid(androidVariantConf)
    }

    def 'test checkIfApphancePresent no apphance'() {
        expect:
        !addApphanceToAndroid.checkIfApphancePresent()
    }

    def 'test checkIfApphancePresent startNewSession'() {
        given:
        copyFile(new File('src/test/resources/com/apphance/flow/android/TestActivity.java'), new File(variantDir,
                'src/com/apphance/flowTest/android/TestActivity.java'))

        expect:
        addApphanceToAndroid.checkIfApphancePresent()
    }

    def 'test checkIfApphancePresent apphance jar'() {
        given:
        new File(variantDir, 'libs/apphance-library.jar').createNewFile()

        expect:
        addApphanceToAndroid.checkIfApphancePresent()
    }

    def 'test checkIfApphancePresent apphance activity'() {
        given:
        copyFile(new File('src/test/resources/com/apphance/flow/android/AndroidManifestWithProblemActivity.xml'), new File(variantDir, 'AndroidManifest.xml'))

        expect:
        addApphanceToAndroid.checkIfApphancePresent()
    }

    def 'test addReportActivityToManifest'() {
        given:
        addApphanceToAndroid.addProblemActivityToManifest()
        def manifestFile = new File(variantDir, 'AndroidManifest.xml')
        def manifest = new XmlSlurper().parse(manifestFile).declareNamespace(android: 'http://schemas.android.com/apk/res/android');

        expect:
        manifest.application.activity.find { GPathResult it ->
            ['android:name': 'com.apphance.android.ui.ProblemActivity',
                    'android:configChanges': 'orientation',
                    'android:launchMode': 'singleInstance',
                    'android:process': 'com.utest.apphance.reporteditor'].every { key, val ->
                it."@$key".text() == val
            }
        }
    }

    def 'test addPermisions'() {
        given:
        addApphanceToAndroid.addPermisions()
        def manifestFile = new File(variantDir, 'AndroidManifest.xml')
        def manifest = new XmlSlurper().parse(manifestFile).declareNamespace(android: 'http://schemas.android.com/apk/res/android');

        expect:
        manifest.'uses-permission'.size() == 9
        manifest.'uses-permission'.collect { it.'@android:name'.text() } ==
                [INTERNET, CHANGE_WIFI_STATE, READ_PHONE_STATE, GET_TASKS, ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE, ACCESS_COARSE_LOCATION,
                        ACCESS_FINE_LOCATION, BLUETOOTH]
    }

    def 'test addStartNewSessionToAllMainActivities'() {
        given:
        File mainActivity = new File(variantDir, 'src/com/apphance/flowTest/android/TestActivity.java')
        def appKeyCond = { mainActivity.text.contains 'public static final String APP_KEY = "TestKey";' }
        def startNewSessionCond = { mainActivity.text.contains 'Apphance.startNewSession(this, APP_KEY, Apphance.Mode.QA);' }

        expect:
        mainActivity.exists()
        !appKeyCond()
        !startNewSessionCond()

        when:
        addApphanceToAndroid.addStartNewSessionToAllMainActivities()

        then:
        appKeyCond()
        startNewSessionCond()
    }

    def 'test addStartStopInvocations'() {
        when:
        addApphanceToAndroid.addStartStopInvocations(activity)

        then:
        removeWhitespace(activity.text).contains(removeWhitespace("""
            |protected void onStart() {
            |    super.onStart();
            |    Apphance.onStart(this);
            |}
            |""".stripMargin()))

        removeWhitespace(activity.text).contains(removeWhitespace("""
            |protected void onStop() {
            |    super.onStop();
            |    Apphance.onStop(this);
            |}
            |""".stripMargin()))

        where:
        activity                                                                                               | _
        tempFile << TEST_ACTIVITY.text                                                                         | _
        tempFile << new File('src/test/resources/com/apphance/flow/android/TestActivityWithOnStart.java').text | _
    }

    def 'test get public class activity'() {
        given:
        def file = new File('src/test/resources/com/apphance/flow/android/TestActivityManyClasses.java')

        expect:
        addApphanceToAndroid.getActivity(file).name == 'TestActivityManyClasses'
    }

    def 'test aphance import added'() {
        given:
        def testActivity = tempFile << TEST_ACTIVITY.text

        when:
        addApphanceToAndroid.addApphanceImportTo(testActivity)

        then:
        contains(testActivity, APPHANCE_IMPORT)
    }

    def 'test apphance log'() {
        given:
        def testActivity = tempFile << TEST_ACTIVITY.text

        when:
        addApphanceToAndroid.convertLogToApphance(testActivity)

        then:
        contains(testActivity, APPHANCE_LOG_IMPORT)
        !contains(testActivity, 'android.util.Log')
    }

    def 'test addApphanceLibToProjectProperties'() {
        expect:
        !contains(new File(variantDir, 'project.properties'), 'libs/apphance-library-1.9-RC1')

        when:
        addApphanceToAndroid.addApphanceLibraryReferenceToProjectProperties()

        then:
        contains(new File(variantDir, 'project.properties'), 'android.library.reference.2=libs/apphance-library-1.9-RC1')
    }
}
