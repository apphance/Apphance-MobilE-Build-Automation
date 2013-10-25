package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceMode
import com.apphance.flow.util.FlowUtils
import com.google.common.io.Files
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.io.FileUtils
import spock.lang.Specification
import spock.lang.Unroll

import static android.Manifest.permission.*
import static com.apphance.flow.configuration.apphance.ApphanceMode.QA
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
        FileUtils.copyDirectory(new File('projects/test/android/android-basic'), variantDir)

        androidVariantConf.aphMode.value = QA
        androidVariantConf.aphAppKey.value = 'TestKey'
        androidVariantConf.aphLib.value = '1.9-RC4'
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
        addApphanceToAndroid.addPermissions()
        def manifestFile = new File(variantDir, 'AndroidManifest.xml')
        def manifest = new XmlSlurper().parse(manifestFile).declareNamespace(android: 'http://schemas.android.com/apk/res/android');

        expect:
        manifest.'uses-permission'.size() == 9
        manifest.'uses-permission'.collect { it.'@android:name'.text() }.sort() ==
                [INTERNET, CHANGE_WIFI_STATE, READ_PHONE_STATE, GET_TASKS, ACCESS_WIFI_STATE, ACCESS_NETWORK_STATE, ACCESS_COARSE_LOCATION,
                        ACCESS_FINE_LOCATION, BLUETOOTH].sort()
    }

    def 'test addStartNewSessionToAllMainActivities'() {
        given:
        File mainActivity = new File(variantDir, 'src/com/apphance/flowTest/android/TestActivity.java')
        def startSession = 'Apphance.startNewSession(this, "TestKey", Apphance.Mode.QA);'
        addApphanceToAndroid.apphanceVersion = '1.9'

        expect:
        mainActivity.exists()
        !mainActivity.text.contains(startSession)

        when:
        addApphanceToAndroid.addStartNewSessionToAllMainActivities()

        then:
        mainActivity.text.contains startSession
    }

    def 'test addStartNewSessionToAllMainActivities source without onCreate method'() {
        given:
        File mainActivity = tempFile << new File('src/test/resources/com/apphance/flow/android/TestActivityWithoutOnCreate.java').text
        def startSession = 'Apphance.startNewSession(this, "TestKey", Apphance.Mode.QA);'

        expect:
        mainActivity.exists()
        !mainActivity.text.contains(startSession)

        when:
        addApphanceToAndroid.addApphanceInit(mainActivity, "TestKey", QA)
        println mainActivity.text

        then:
        mainActivity.text.contains startSession
    }

    @Unroll
    def 'test startNewSession with apphance 1.9.6'() {
        given:
        File mainActivity = tempFile << new File('src/test/resources/com/apphance/flow/android/TestActivityWithoutOnCreate.java').text
        addApphanceToAndroid.apphanceVersion = '1.9.6'

        expect:
        mainActivity.exists()
        !mainActivity.text.contains("com.apphance.android.common.Configuration configuration = new com.apphance.android.common.Configuration.Builder(this)")

        when:
        addApphanceToAndroid.addApphanceInitVer196(mainActivity, "TestKey", QA, false, true, defaultUser, screenshotFromGallery)
        println mainActivity.text

        then:
        mainActivity.text.contains "public void onCreate(Bundle savedInstanceState) {"
        mainActivity.text.contains "super.onCreate(savedInstanceState);"
        mainActivity.text.contains "com.apphance.android.common.Configuration configuration = new com.apphance.android.common.Configuration.Builder(this)"
        mainActivity.text.contains ".withMode(Apphance.Mode.QA)"
        mainActivity.text.contains "Apphance.startNewSession(this, configuration);"
        mainActivity.text.split('\n').toList()*.trim().containsAll lines
        !dontExists.any { String it -> mainActivity.text.contains it }

        where:
        defaultUser        | screenshotFromGallery | lines                                                                                | dontExists
        'user@example.com' | true                  | ['.withDefaultUser("user@example.com")', '.withScreenshotFromGalleryEnabled(true)']  | []
        'user@example.com' | false                 | ['.withDefaultUser("user@example.com")', '.withScreenshotFromGalleryEnabled(false)'] | []
        null               | false                 | ['.withScreenshotFromGalleryEnabled(false)']                                         | ['withDefaultUser']
        null               | true                  | ['.withScreenshotFromGalleryEnabled(true)']                                          | ['withDefaultUser']
    }

    @Unroll
    def 'test use appropriate init method according to lib version. #version'() {
        given:
        def addApphance = GroovySpy(AddApphanceToAndroid)
        addApphance.apphanceVersion = ver
        def file = tempFile

        when:
        addApphance.addStartNewSession(file)

        then:
        1 * addApphance."$method"(* _)

        where:
        ver     | method
        '1.9'   | 'addApphanceInit'
        '1.9.6' | 'addApphanceInitVer196'
        '2.0'   | 'addApphanceInitVer196'
    }

    def 'test addStartStopInvocations'() {
        when:
        addApphanceToAndroid.addStartStopInvocations(activity)

        then:
        true

        then:
        removeWhitespace(activity.text).contains(removeWhitespace("""
            |protected void onStart() {
            |    Apphance.onStart(this);
            |    super.onStart();
            |}
            |""".stripMargin()))

        removeWhitespace(activity.text).contains(removeWhitespace("""
            |protected void onStop() {
            |    Apphance.onStop(this);
            |    super.onStop();
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

    @Unroll
    def 'test apphance import added. #libVersion'() {
        given:
        def testActivity = tempFile << TEST_ACTIVITY.text
        def before = testActivity.readLines().findAll { it.contains('import') }.size()
        addApphanceToAndroid.apphanceVersion = apphanceVersion

        when:
        addApphanceToAndroid.addApphanceImportTo(testActivity)

        then:
        testActivity.readLines().findAll { it.contains('import') }.size() == before + 1
        testActivity.readLines().findAll { it.contains('import com.apphance') }*.trim().sort() == [APPHANCE_IMPORT].sort()

        where:
        apphanceVersion << ['1.9.5', '1.9.6']
    }

    def 'test apphance log'() {
        given:
        def testActivity = tempFile << TEST_ACTIVITY.text

        when:
        addApphanceToAndroid.convertLogToApphanceInFile(testActivity)

        then:
        contains(testActivity, APPHANCE_LOG_IMPORT)
        !contains(testActivity, 'android.util.Log')
    }

    def 'test addApphanceLibToProjectProperties'() {
        expect:
        !contains(new File(variantDir, 'project.properties'), 'libs/apphance-library-1.9-RC4')

        when:
        addApphanceToAndroid.addApphanceLibraryReferenceToProjectProperties()

        then:
        contains(new File(variantDir, 'project.properties'), 'android.library.reference.2=libs/apphance-library-1.9-RC4')
    }

    def 'test adding problem activity after addStartStopInvocations'() {
        given:
        def addApphance = Spy(AddApphanceToAndroid, constructorArgs: [temporaryDir, 'KEY', QA, '1.9', false, null, false])

        when:
        addApphance.addApphance()

        then:
        1 * addApphance.checkIfApphancePresent() >> false
        1 * addApphance.addApphanceImportsAndStartStopMethodsInAllActivities() >> null
        1 * addApphance.addStartNewSessionToAllMainActivities() >> null
        1 * addApphance.convertLogToApphance() >> null

        then:
        1 * addApphance.addProblemActivityToManifest() >> null
        1 * addApphance.addPermissions() >> null
        1 * addApphance.addApphanceLib()
        1 * addApphance.downloadZipAndUnzip(* _) >> null
        1 * addApphance.addApphanceLibraryReferenceToProjectProperties() >> null
    }

    def 'test addApphance in production mode'() {
        given:
        androidVariantConf.aphMode.value = ApphanceMode.PROD
        def addApphance = Spy(AddApphanceToAndroid, constructorArgs: [androidVariantConf])
        addApphance.addApphanceLib() >> null
        def manifest = new File(variantDir, 'AndroidManifest.xml')

        when:
        addApphance.addApphance()

        then:
        !manifest.text.contains('ProblemActivity')
        !manifest.text.contains('READ_PHONE_STATE')
        !manifest.text.contains('GET_TASKS')
    }

    def 'test annotations'() {
        given:
        def testActivity = tempFile << new File('src/test/resources/com/apphance/flow/android/NoApphanceActivity.java').text

        when:
        addApphanceToAndroid.addApphanceImportTo(testActivity)
        println testActivity.text

        then:
        testActivity.text.contains('@Override')
        contains(testActivity, APPHANCE_IMPORT)
    }

    def 'test isOnCreatePresent'() {
        expect:
        !addApphanceToAndroid.isMethodPresent(new File('src/test/resources/com/apphance/flow/android/TestActivityWithoutOnCreate.java'), 'onCreate')
        addApphanceToAndroid.isMethodPresent(new File('src/test/resources/com/apphance/flow/android/TestActivity.java'), 'onCreate')
    }

    def 'test not adding apphance if already present'() {
        given:
        def addApphance = GroovySpy(AddApphanceToAndroid)
        addApphance.checkIfApphancePresent() >> true

        when:
        addApphance.addApphance()

        then:
        0 * addApphance.addStartNewSessionToAllMainActivities()
        0 * addApphance.addApphanceImportsAndStartStopMethodsInAllActivities()
        0 * addApphance.addProblemActivityToManifest()
        0 * addApphance.addPermissions()
        0 * addApphance.addApphanceLib()
        0 * addApphance.addApphanceLibraryReferenceToProjectProperties()
    }

    def 'test send log calls to apphance'() {
        when:
        addApphanceToAndroid.convertLogToApphance()

        then:
        !new File(variantDir, 'src/com/apphance/flowTest/android/SomeClass.java').text.contains('android.util.Log')
        new File(variantDir, 'src/com/apphance/flowTest/android/SomeClass.java').text.contains 'import com.apphance.android.Log;'
    }

    @Unroll
    def 'test lib version #ver, #compare #result'() {
        given:
        addApphanceToAndroid.apphanceVersion = ver

        expect:
        addApphanceToAndroid.libVerLowerThan(compare) == result

        where:
        ver       | compare     | result
        '1.9.5'   | '1.9.5'     | false
        '1.9.5'   | '1.9.6'     | true
        '1.9'     | '1.9.6'     | true
        '1'       | '1.9.6'     | true
        '2'       | '1.9.6'     | false
        '2.1'     | '1.9.6'     | false
        '2.1.1'   | '1.9.6'     | false
        '2.1.1'   | '1.9.6'     | false
        '2.0-RC1' | '1.9.9.9.9' | false
        '2.1-M2'  | '1.9.9.9.9' | false
        '2'       | '1-RC2'     | false
    }
}
