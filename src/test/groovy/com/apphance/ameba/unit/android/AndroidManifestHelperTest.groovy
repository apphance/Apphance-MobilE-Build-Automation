package com.apphance.ameba.unit.android

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.android.AndroidManifestHelper
import com.sun.org.apache.xpath.internal.XPathAPI
import org.junit.Before
import org.junit.Test
import org.w3c.dom.Element

import javax.xml.parsers.DocumentBuilderFactory

import static org.junit.Assert.*

class AndroidManifestHelperTest {

    AndroidManifestHelper manifestHelper
    File tmpDir

    @Before
    void setUp() {
        this.manifestHelper = new AndroidManifestHelper()
        this.tmpDir = new File("tmp")
        tmpDir.mkdir()
        def androidManifest = new File(tmpDir, "AndroidManifest.xml")
        def originalAndroidManifest = new File("testProjects/android/android-basic/AndroidManifest.xml")
        androidManifest.delete()
        androidManifest << originalAndroidManifest.text
    }

    @Test
    void testReadingVersion() {
        ProjectConfiguration projectConfiguration = new ProjectConfiguration()
        projectConfiguration.updateVersionDetails(manifestHelper.readVersion(tmpDir))
        assertEquals(42, projectConfiguration.versionCode)
        assertEquals('1.0.1', projectConfiguration.versionString)
    }

    @Test
    void testUpdateVersion() {
        def newVersionCode = 1234L
        def newVersionString = '2.0.3'

        ProjectConfiguration projectConfiguration = new ProjectConfiguration()
        manifestHelper.restoreOriginalManifest(tmpDir)
        projectConfiguration.updateVersionDetails(manifestHelper.readVersion(tmpDir))
        projectConfiguration.setVersionString('2.0.3')
        manifestHelper.updateVersion(tmpDir, new Expando(versionCode: newVersionCode, versionString: newVersionString))
        try {
            ProjectConfiguration projectConfiguration2 = new ProjectConfiguration()
            projectConfiguration2.updateVersionDetails(manifestHelper.readVersion(tmpDir))
            assertEquals(newVersionCode, projectConfiguration2.versionCode)
            assertEquals(newVersionString, projectConfiguration2.versionString)
        } finally {
            manifestHelper.restoreOriginalManifest(tmpDir)
        }
    }

    @Test
    void testRemoveApphanceOnlyAndRestore() {
        manifestHelper.restoreOriginalManifest(tmpDir)
        def file = new File(tmpDir, "AndroidManifest.xml")
        String originalText = file.text
        verifyApphanceIsPresent()
        manifestHelper.removeApphance(tmpDir)
        def origFile = new File(tmpDir, "AndroidManifest.xml.orig")
        try {
            verifyApphanceIsRemoved()
            assertTrue(origFile.exists())
        } finally {
            manifestHelper.restoreOriginalManifest(tmpDir)
        }
        assertFalse(origFile.exists())
        def fileAgain = new File(tmpDir, "AndroidManifest.xml")
        assertEquals(originalText, fileAgain.text)
    }

    private verifyApphanceIsPresent() {
        def rootOrig = getParsedManifest(tmpDir)
        assertEquals(1, XPathAPI.selectNodeList(rootOrig, '/manifest/application/activity[@name="TestActivity"]').length)
        assertEquals(1, XPathAPI.selectNodeList(rootOrig, '/manifest/uses-permission[@name="android.permission.INTERNET"]').length)
        assertEquals(1, XPathAPI.selectNodeList(rootOrig, '/manifest/application/activity[@name="AnotherActivity"]').length)
        assertEquals(1, XPathAPI.selectNodeList(rootOrig, '/manifest/application/activity-alias[@name="AliasTestActivity"]').length)
        assertEquals(1, XPathAPI.selectNodeList(rootOrig, '/manifest/uses-permission[@name="android.permission.CHANGE_WIFI_STATE"]').length)
        assertEquals(1, XPathAPI.selectNodeList(rootOrig, '/manifest/instrumentation[@name="com.apphance.android.ApphanceInstrumentation"]').length)
        assertEquals(1, XPathAPI.selectNodeList(rootOrig, '/manifest/application/activity/intent-filter/action[@name=\'com.apphance.android.LAUNCH\']').length)
        assertEquals(1, XPathAPI.selectNodeList(rootOrig, '/manifest/application/activity-alias/intent-filter/action[@name=\'android.intent.action.MAIN\']').length)
        assertEquals(1, XPathAPI.selectNodeList(rootOrig, '/manifest/application/activity-alias/intent-filter/category[@name=\'android.intent.category.LAUNCHER\']').length)
        assertEquals(0, XPathAPI.selectNodeList(rootOrig, '/manifest/application/activity/intent-filter/action[@name=\'android.intent.action.MAIN\']').length)
        assertEquals(0, XPathAPI.selectNodeList(rootOrig, '/manifest/application/activity/intent-filter/category[@name=\'android.intent.category.LAUNCHER\']').length)
    }

    private verifyApphanceIsRemoved() {
        def root = getParsedManifest(tmpDir)
        assertEquals(1, XPathAPI.selectNodeList(root, '/manifest/application/activity[@name="TestActivity"]').length)
        assertEquals(1, XPathAPI.selectNodeList(root, '/manifest/uses-permission[@name="android.permission.INTERNET"]').length)
        assertEquals(0, XPathAPI.selectNodeList(root, '/manifest/application/activity[@name="AnotherActivity"]').length)
        assertEquals(0, XPathAPI.selectNodeList(root, '/manifest/application/activity-alias[@name="AliasTestActivity"]').length)
        assertEquals(0, XPathAPI.selectNodeList(root, '/manifest/application/activity-alias[@name="AliasTestActivity"]').length)
        assertEquals(0, XPathAPI.selectNodeList(root, '/manifest/uses-permission[@name="android.permission.CHANGE_WIFI_STATE"]').length)
        assertEquals(0, XPathAPI.selectNodeList(root, '/manifest/instrumentation[@name="com.apphance.android.ApphanceInstrumentation"]').length)
        assertEquals(0, XPathAPI.selectNodeList(root, '/manifest/application/activity/intent-filter/action[@name=\'com.apphance.android.LAUNCH\']').length)
        assertEquals(0, XPathAPI.selectNodeList(root, '/manifest/application/activity-alias/intent-filter/action[@name=\'android.intent.action.MAIN\']').length)
        assertEquals(0, XPathAPI.selectNodeList(root, '/manifest/application/activity-alias/intent-filter/category[@name=\'android.intent.category.LAUNCHER\']').length)
        assertEquals(1, XPathAPI.selectNodeList(root, '/manifest/application/activity/intent-filter/action[@name=\'android.intent.action.MAIN\']').length)
        assertEquals(1, XPathAPI.selectNodeList(root, '/manifest/application/activity/intent-filter/category[@name=\'android.intent.category.LAUNCHER\']').length)
        String text = new File(tmpDir, "AndroidManifest.xml").text.toLowerCase()
    }

    @Test
    void testReplacePackageOnly() {
        manifestHelper.restoreOriginalManifest(tmpDir)
        def file = new File(tmpDir, "AndroidManifest.xml")
        String originalText = file.text
        manifestHelper.replacePackage(tmpDir, 'com.apphance.amebaTest.android',
                'com.apphance.amebaTest.android.new', null)
        def origFile = new File(tmpDir, "AndroidManifest.xml.orig")
        try {
            def root = getParsedManifest(tmpDir)
            assertEquals(1, XPathAPI.selectNodeList(root, '/manifest[@package="com.apphance.amebaTest.android.new"]').length)
            assertEquals(0, XPathAPI.selectNodeList(root, '/manifest/application[@label="newLabel"]').length)
            assertEquals(1, XPathAPI.selectNodeList(root, '/manifest/application[@label="@string/app_name"]').length)
            assertTrue(origFile.exists())
        } finally {
            manifestHelper.restoreOriginalManifest(tmpDir)
        }
        assertFalse(origFile.exists())
        def fileAgain = new File(tmpDir, "AndroidManifest.xml")
        assertEquals(originalText, fileAgain.text)
    }

    @Test
    void testAddPermissions() {
        manifestHelper.restoreOriginalManifest(tmpDir)
        def file = new File(tmpDir, "AndroidManifest.xml")
        String originalText = file.text
        manifestHelper.addPermissionsToManifest(tmpDir, 'android.permission.ACCESS_MOCK_LOCATION')
        def origFile = new File(tmpDir, "AndroidManifest.xml.orig")
        try {
            def root = getParsedManifest(tmpDir)
            assertEquals(1, XPathAPI.selectNodeList(root, '/manifest/uses-permission[@name="android.permission.ACCESS_MOCK_LOCATION"]').length)
            assertTrue(origFile.exists())
        } finally {
            manifestHelper.restoreOriginalManifest(tmpDir)
        }
        assertFalse(origFile.exists())
        def fileAgain = new File(tmpDir, "AndroidManifest.xml")
        assertEquals(originalText, fileAgain.text)
    }


    @Test
    void testReplacePackageAndLabel() {
        manifestHelper.restoreOriginalManifest(tmpDir)
        def file = new File(tmpDir, "AndroidManifest.xml")
        String originalText = file.text
        manifestHelper.replacePackage(tmpDir, 'com.apphance.amebaTest.android',
                'com.apphance.amebaTest.android.new', 'newLabel')
        def origFile = new File(tmpDir, "AndroidManifest.xml.orig")
        try {
            def root = getParsedManifest(tmpDir)
            assertEquals(1, XPathAPI.selectNodeList(root, '/manifest[@package="com.apphance.amebaTest.android.new"]').length)
            assertEquals(1, XPathAPI.selectNodeList(root, '/manifest/application[@label="newLabel"]').length)
            assertTrue(origFile.exists())
        } finally {
            manifestHelper.restoreOriginalManifest(tmpDir)
        }
        assertFalse(origFile.exists())
        def fileAgain = new File(tmpDir, "AndroidManifest.xml")
        assertEquals(originalText, fileAgain.text)
    }


    @Test
    void testReadMainActivityFromManifest() {
        def file = new File("testProjects/apphance-updates/")
        String mainActivity = manifestHelper.getMainActivityName(file)
        assertEquals(mainActivity, 'pl.morizon.client.ui.HomeActivity')
    }

    @Test
    void testPackage() {
        manifestHelper.restoreOriginalManifest(tmpDir)
        String pkg = manifestHelper.androidPackage(tmpDir)
        assertEquals('com.apphance.amebaTest.android', pkg)
    }

    private Element getParsedManifest(File projectDir) {
        def builderFactory = DocumentBuilderFactory.newInstance()
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        builderFactory.setFeature("http://xml.org/sax/features/validation", false)
        def builder = builderFactory.newDocumentBuilder()
        def inputStream = new FileInputStream(new File(projectDir, 'AndroidManifest.xml'))
        return builder.parse(inputStream).documentElement
    }
}
