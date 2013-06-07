package com.apphance.flow.plugins.android.apphance.tasks

import spock.lang.Specification

import static java.lang.System.getProperties

class AndroidLogsConversionTaskSpec extends Specification {

    def 'converts apphance logs to android'() {

        given:
        def ant = new AntBuilder()
        def logConverter = new AndroidLogsConversionTask(ant)

        and:
        def filenameWithLogs = 'ApphanceToAndroidWithLogs.java'
        def filenameWithoutLogs = 'ApphanceToAndroidWithoutLogs.java'

        and:
        def tmpDir = new File(properties['java.io.tmpdir'].toString(), 'src')
        tmpDir.mkdirs()

        and:
        def classWithLogs = new File(getClass().getResource(
                "${filenameWithLogs}src").file)
        def classWithoutLogs = new File(getClass().getResource(
                "${filenameWithoutLogs}src").file)

        and:
        ant.copy(
                file: classWithLogs.canonicalPath,
                toFile: new File(tmpDir.canonicalPath, filenameWithLogs))
        ant.copy(
                file: classWithoutLogs.canonicalPath,
                toFile: new File(tmpDir.canonicalPath, filenameWithoutLogs))

        when:
        logConverter.convertLogsToAndroid(tmpDir.parentFile)

        then:
        !(new File(tmpDir, filenameWithLogs).text).contains('com.apphance.android.Log')
        (new File(tmpDir, filenameWithLogs).text).contains('android.util.Log')
        !(new File(tmpDir, filenameWithoutLogs).text).contains('com.apphance.android.Log')
        tmpDir.deleteDir()
        !tmpDir.exists()
    }
}
