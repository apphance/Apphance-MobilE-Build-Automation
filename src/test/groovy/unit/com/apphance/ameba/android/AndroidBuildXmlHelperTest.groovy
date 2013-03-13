package com.apphance.ameba.android

import com.apphance.ameba.android.AndroidBuildXmlHelper
import org.junit.Test

import static org.junit.Assert.assertEquals

class AndroidBuildXmlHelperTest {

    private static File PROJECT_DIR = new File('testProjects/android/android-basic')

    @Test
    void testReadNameFromBuildXml() {
        AndroidBuildXmlHelper helper = new AndroidBuildXmlHelper()
        assertEquals('TestAndroidProject', helper.projectName(PROJECT_DIR))
    }

    @Test
    void testReplaceProjectNameInBuildXml() {
        File buildXml = new File('tmp/build.xml')
        File tmpDir = new File('tmp')
        buildXml.delete()
        tmpDir.mkdirs()
        buildXml << new File(PROJECT_DIR, 'build.xml').text
        AndroidBuildXmlHelper helper = new AndroidBuildXmlHelper()
        assertEquals('TestAndroidProject', helper.projectName(new File('tmp')))
        helper.replaceProjectName(new File('tmp'), 'NewName')
        assertEquals('NewName', helper.projectName(new File('tmp')))
    }
}
