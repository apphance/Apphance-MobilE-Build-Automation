package com.apphance.ameba.plugins.android

import org.apache.commons.io.FileUtils
import spock.lang.Specification

class AddApphanceSpec extends Specification{

    File noApphanceNoApplicationDirectory = new File('testProjects/android/android-no-apphance-no-application')
    File tmpDir = new File('tmp/testApphance')
    def helper = new AndroidManifestHelper()

    private void copySources(File source, File destination) {
        FileUtils.deleteDirectory(destination)
        destination.mkdirs()
        new AntBuilder().copy(todir: destination) { fileset(dir: source) }
    }

    def "add manifest test"() {
        given:
        copySources(noApphanceNoApplicationDirectory, tmpDir)

        expect:
        !helper.isApphanceInstrumentationPresent(noApphanceNoApplicationDirectory)

        when:
        helper.addApphance(tmpDir)
        def manifest = new XmlSlurper().parse(new File(tmpDir, 'AndroidManifest.xml'))
        def getTasks = manifest."uses-permission".findAll { it.@'android:name'.equals("android.permission.GET_TASKS") }
        def readPhone = manifest."uses-permission".findAll { it.@'android:name'.equals("android.permission.READ_PHONE_STATE") }

        then:
        helper.isApphanceInstrumentationPresent(tmpDir)
        1 == getTasks.size()
        1 == readPhone.size()
    }

    def "check Apphance instrumentation"() {
        expect:
        AndroidManifestHelper.isApphanceInstrumentationPresent(new File('testProjects/android/android-basic'))
    }
}
