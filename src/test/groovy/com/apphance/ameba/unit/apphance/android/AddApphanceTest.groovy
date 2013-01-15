package com.apphance.ameba.unit.apphance.android

import com.apphance.ameba.android.AndroidManifestHelper
import groovy.util.slurpersupport.GPathResult
import org.junit.Test

import static org.junit.Assert.*

class AddApphanceTest {

    File noApphanceNoApplicationDirectory = new File('testProjects/android-no-apphance-no-application')
    File tmpDir = new File('tmp/testApphance')

    private def deleteRecursive(File f) {
        if (f.exists()) {
            f.eachDir({ deleteRecursive(it) })
            f.eachFile { it.delete() }
        }
    }

    private void copySources(File source, File destination) {
        deleteRecursive(destination)
        destination.mkdirs()
        new AntBuilder().copy(todir: destination) { fileset(dir: source) }
    }

    @Test
    void addManifestTest() {
        copySources(noApphanceNoApplicationDirectory, tmpDir)
        AndroidManifestHelper helper = new AndroidManifestHelper()
        assertFalse(helper.isApphanceInstrumentationPresent(noApphanceNoApplicationDirectory))
        File androidManifest = new File(tmpDir, 'AndroidManifest.xml')
        helper.addApphanceToManifest(tmpDir)
        XmlSlurper slurper = new XmlSlurper()
        GPathResult manifest = slurper.parse(androidManifest)
        def getTasks = manifest."uses-permission".findAll {
            it.@'android:name'.equals("android.permission.GET_TASKS")
        }
        assertEquals(1, getTasks.size())

        def readPhone = manifest."uses-permission".findAll {
            it.@'android:name'.equals("android.permission.READ_PHONE_STATE")
        }
        assertEquals(1, readPhone.size())
        assertTrue(helper.isApphanceInstrumentationPresent(tmpDir))
    }

    @Test
    void checkApphanceInstrumentation() {
        AndroidManifestHelper helper = new AndroidManifestHelper()
        assertTrue(helper.isApphanceInstrumentationPresent(new File('testProjects/android/')))
    }
}
