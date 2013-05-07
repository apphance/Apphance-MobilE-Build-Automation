package com.apphance.ameba.plugins.android

import com.sun.org.apache.xpath.internal.XPathAPI
import org.junit.Before
import org.junit.Test
import org.w3c.dom.Element

import javax.xml.parsers.DocumentBuilderFactory

import static org.junit.Assert.*

//TODO rewrite to spock!
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
    void testAddPermissions() {
        manifestHelper.restoreOriginalManifest(tmpDir)
        def file = new File(tmpDir, "AndroidManifest.xml")
        String originalText = file.text
        manifestHelper.addPermissions(tmpDir, 'android.permission.ACCESS_MOCK_LOCATION')
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

    private Element getParsedManifest(File projectDir) {
        def builderFactory = DocumentBuilderFactory.newInstance()
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        builderFactory.setFeature("http://xml.org/sax/features/validation", false)
        def builder = builderFactory.newDocumentBuilder()
        def inputStream = new FileInputStream(new File(projectDir, 'AndroidManifest.xml'))
        return builder.parse(inputStream).documentElement
    }
}
