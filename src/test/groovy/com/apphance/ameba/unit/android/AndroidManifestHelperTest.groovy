package com.apphance.ameba.unit.android;

import static org.junit.Assert.*

import org.junit.AfterClass
import org.junit.Before
import org.junit.Test

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.android.AndroidManifestHelper
import com.sun.org.apache.xpath.internal.XPathAPI

class AndroidManifestHelperTest {

    AndroidManifestHelper manifestHelper
    File tmpDir

    @Before
    void setUp() {
        this.manifestHelper = new AndroidManifestHelper()
        this.tmpDir = new File("tmp")
        tmpDir.mkdir()
        def androidManifest = new File(tmpDir,"AndroidManifest.xml")
        def originalAndroidManifest = new File("testProjects/android/AndroidManifest.xml")
        androidManifest.delete()
        androidManifest << originalAndroidManifest.text
    }

    @Test
    void testReadingVersion() {
        ProjectConfiguration projectConfiguration = new ProjectConfiguration()
        manifestHelper.readVersion(tmpDir, projectConfiguration)
        assertEquals(42, projectConfiguration.versionCode)
        assertEquals('1.0.1', projectConfiguration.versionString)
    }

    @Test
    void testUpdateVersion() {
        ProjectConfiguration projectConfiguration = new ProjectConfiguration()
        manifestHelper.restoreOriginalManifest(tmpDir)
        manifestHelper.readVersion(tmpDir, projectConfiguration)
        projectConfiguration.setVersionString("2.0.3")
        manifestHelper.updateVersion(tmpDir, projectConfiguration)
        try {
            ProjectConfiguration projectConfiguration2 = new ProjectConfiguration()
            manifestHelper.readVersion(tmpDir, projectConfiguration2)
            assertEquals(43, projectConfiguration2.versionCode)
            assertEquals('2.0.3', projectConfiguration2.versionString)
        } finally {
            manifestHelper.restoreOriginalManifest(tmpDir)
        }
    }

    @Test
    void testRemoveApphanceOnlyAndRestore() {
        ProjectConfiguration projectConfiguration = new ProjectConfiguration()
        manifestHelper.restoreOriginalManifest(tmpDir)
        def file = new File(tmpDir,"AndroidManifest.xml")
        String originalText = file.text
        verifyApphanceIsPresent()
        manifestHelper.removeApphance(tmpDir)
        def origFile = new File(tmpDir,"AndroidManifest.xml.orig")
        try {
            verifyApphanceIsRemoved()
            assertTrue(origFile.exists())
        } finally {
            manifestHelper.restoreOriginalManifest(tmpDir)
        }
        assertFalse(origFile.exists())
        def fileAgain = new File(tmpDir,"AndroidManifest.xml")
        assertEquals(originalText, fileAgain.text)
    }

    private verifyApphanceIsPresent() {
        def rootOrig = manifestHelper.getParsedManifest(tmpDir)
        assertEquals(1,XPathAPI.selectNodeList(rootOrig,'/manifest/application/activity[@name="TestActivity"]').length)
        assertEquals(1,XPathAPI.selectNodeList(rootOrig,'/manifest/uses-permission[@name="android.permission.INTERNET"]').length)
        assertEquals(1,XPathAPI.selectNodeList(rootOrig,'/manifest/application/activity[@name="AnotherActivity"]').length)
        assertEquals(1,XPathAPI.selectNodeList(rootOrig,'/manifest/application/activity-alias[@name="AliasTestActivity"]').length)
        assertEquals(1,XPathAPI.selectNodeList(rootOrig,'/manifest/uses-permission[@name="android.permission.CHANGE_WIFI_STATE"]').length)
        assertEquals(1,XPathAPI.selectNodeList(rootOrig,'/manifest/instrumentation[@name="com.apphance.android.ApphanceInstrumentation"]').length)
        assertEquals(1,XPathAPI.selectNodeList(rootOrig,'/manifest/application/activity/intent-filter/action[@name=\'com.apphance.android.LAUNCH\']').length)
        assertEquals(1,XPathAPI.selectNodeList(rootOrig,'/manifest/application/activity-alias/intent-filter/action[@name=\'android.intent.action.MAIN\']').length)
        assertEquals(1,XPathAPI.selectNodeList(rootOrig,'/manifest/application/activity-alias/intent-filter/category[@name=\'android.intent.category.LAUNCHER\']').length)
        assertEquals(0,XPathAPI.selectNodeList(rootOrig,'/manifest/application/activity/intent-filter/action[@name=\'android.intent.action.MAIN\']').length)
        assertEquals(0,XPathAPI.selectNodeList(rootOrig,'/manifest/application/activity/intent-filter/category[@name=\'android.intent.category.LAUNCHER\']').length)
    }

    private verifyApphanceIsRemoved() {
        def root = manifestHelper.getParsedManifest(tmpDir)
        assertEquals(1,XPathAPI.selectNodeList(root,'/manifest/application/activity[@name="TestActivity"]').length)
        assertEquals(1,XPathAPI.selectNodeList(root,'/manifest/uses-permission[@name="android.permission.INTERNET"]').length)
        assertEquals(0,XPathAPI.selectNodeList(root,'/manifest/application/activity[@name="AnotherActivity"]').length)
        assertEquals(0,XPathAPI.selectNodeList(root,'/manifest/application/activity-alias[@name="AliasTestActivity"]').length)
        assertEquals(0,XPathAPI.selectNodeList(root,'/manifest/application/activity-alias[@name="AliasTestActivity"]').length)
        assertEquals(0,XPathAPI.selectNodeList(root,'/manifest/uses-permission[@name="android.permission.CHANGE_WIFI_STATE"]').length)
        assertEquals(0,XPathAPI.selectNodeList(root,'/manifest/instrumentation[@name="com.apphance.android.ApphanceInstrumentation"]').length)
        assertEquals(0,XPathAPI.selectNodeList(root,'/manifest/application/activity/intent-filter/action[@name=\'com.apphance.android.LAUNCH\']').length)
        assertEquals(0,XPathAPI.selectNodeList(root,'/manifest/application/activity-alias/intent-filter/action[@name=\'android.intent.action.MAIN\']').length)
        assertEquals(0,XPathAPI.selectNodeList(root,'/manifest/application/activity-alias/intent-filter/category[@name=\'android.intent.category.LAUNCHER\']').length)
        assertEquals(1,XPathAPI.selectNodeList(root,'/manifest/application/activity/intent-filter/action[@name=\'android.intent.action.MAIN\']').length)
        assertEquals(1,XPathAPI.selectNodeList(root,'/manifest/application/activity/intent-filter/category[@name=\'android.intent.category.LAUNCHER\']').length)
        String text = new File(tmpDir,"AndroidManifest.xml").text.toLowerCase()
    }

    @Test
    void testReplacePackageOnly() {
        ProjectConfiguration projectConfiguration = new ProjectConfiguration()
        manifestHelper.restoreOriginalManifest(tmpDir)
        def file = new File(tmpDir,"AndroidManifest.xml")
        String originalText = file.text
        manifestHelper.replacePackage(tmpDir, projectConfiguration, 'com.apphance.amebaTest.android',
                        'com.apphance.amebaTest.android.new',null)
        def origFile = new File(tmpDir,"AndroidManifest.xml.orig")
        try {
            def root = manifestHelper.getParsedManifest(tmpDir)
            assertEquals(1,XPathAPI.selectNodeList(root,'/manifest[@package="com.apphance.amebaTest.android.new"]').length)
            assertEquals(0,XPathAPI.selectNodeList(root,'/manifest/application[@label="newLabel"]').length)
            assertEquals(1,XPathAPI.selectNodeList(root,'/manifest/application[@label="@string/app_name"]').length)
            assertTrue(origFile.exists())
        } finally {
            manifestHelper.restoreOriginalManifest(tmpDir)
        }
        assertFalse(origFile.exists())
        def fileAgain = new File(tmpDir,"AndroidManifest.xml")
        assertEquals(originalText, fileAgain.text)
    }

    @Test
    void testAddPermissions() {
        ProjectConfiguration projectConfiguration = new ProjectConfiguration()
        manifestHelper.restoreOriginalManifest(tmpDir)
        def file = new File(tmpDir,"AndroidManifest.xml")
        String originalText = file.text
        manifestHelper.addPermissionsToManifest(tmpDir, ['android.permission.ACCESS_MOCK_LOCATION'])
        def origFile = new File(tmpDir,"AndroidManifest.xml.orig")
        try {
            def root = manifestHelper.getParsedManifest(tmpDir)
            assertEquals(1,XPathAPI.selectNodeList(root,'/manifest/uses-permission[@name="android.permission.ACCESS_MOCK_LOCATION"]').length)
            assertTrue(origFile.exists())
        } finally {
            manifestHelper.restoreOriginalManifest(tmpDir)
        }
        assertFalse(origFile.exists())
        def fileAgain = new File(tmpDir,"AndroidManifest.xml")
        assertEquals(originalText, fileAgain.text)
    }


    @Test
    void testReplacePackageAndLabel() {
        ProjectConfiguration projectConfiguration = new ProjectConfiguration()
        manifestHelper.restoreOriginalManifest(tmpDir)
        def file = new File(tmpDir,"AndroidManifest.xml")
        String originalText = file.text
        manifestHelper.replacePackage(tmpDir, projectConfiguration, 'com.apphance.amebaTest.android',
                        'com.apphance.amebaTest.android.new','newLabel')
        def origFile = new File(tmpDir,"AndroidManifest.xml.orig")
        try {
            def root = manifestHelper.getParsedManifest(tmpDir)
            assertEquals(1,XPathAPI.selectNodeList(root,'/manifest[@package="com.apphance.amebaTest.android.new"]').length)
            assertEquals(1,XPathAPI.selectNodeList(root,'/manifest/application[@label="newLabel"]').length)
            assertTrue(origFile.exists())
        } finally {
            manifestHelper.restoreOriginalManifest(tmpDir)
        }
        assertFalse(origFile.exists())
        def fileAgain = new File(tmpDir,"AndroidManifest.xml")
        assertEquals(originalText, fileAgain.text)
    }


    @Test
    void testReadMainActivityFromManifest() {
        ProjectConfiguration projectConfiguration = new ProjectConfiguration()
        def file = new File("testProjects/apphance-updates/")
        String mainActivity = manifestHelper.getMainActivityName(file)
        assertTrue(mainActivity.contains('HomeActivity'))
    }
}
