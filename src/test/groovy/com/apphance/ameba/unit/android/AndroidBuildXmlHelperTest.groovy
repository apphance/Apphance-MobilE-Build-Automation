package com.apphance.ameba.unit.android;

import static org.junit.Assert.*

import org.junit.Test

import com.apphance.ameba.android.AndroidBuildXmlHelper

class AndroidBuildXmlHelperTest {

    @Test
    void testReadNameFromBuildXml() {
        AndroidBuildXmlHelper helper = new AndroidBuildXmlHelper()
        assertEquals('TestAndroidProject', helper.readProjectName(new File('testProjects/android')))
    }

    @Test
    void testReplaceProjectNameInBuildXml() {
        File buildXml = new File("tmp/build.xml")
        File tmpDir = new File("tmp")
        buildXml.delete()
        tmpDir.delete()
        tmpDir.mkdirs()
        buildXml << new File("testProjects/android/build.xml").text
        AndroidBuildXmlHelper helper = new AndroidBuildXmlHelper()
        assertEquals('TestAndroidProject', helper.readProjectName(new File('tmp')))
        helper.replaceProjectName(new File('tmp'), "NewName")
        assertEquals('NewName', helper.readProjectName(new File('tmp')))
    }
}
