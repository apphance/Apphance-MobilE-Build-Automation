package com.apphance.ameba.plugins.ios.apphance

import com.apphance.ameba.plugins.ios.apphance.tasks.IOSApphanceSourceHelper
import spock.lang.Specification

import static java.lang.System.currentTimeMillis
import static java.lang.System.getProperties

class IOSApphanceSourceHelperSpec extends Specification {

    def 'adds apphance when bracket is either in line or not in line'() {

        given:
        def sourceHelper = new IOSApphanceSourceHelper()

        and:
        def tmpDirName = currentTimeMillis() as String
        def tmpDir = new File(properties['java.io.tmpdir'].toString(), tmpDirName)
        tmpDir.mkdirs()

        and:
        def mFile = new File(baseDir, 'MoviePlayerAppDelegate.m')
        def hFile = new File(baseDir, 'MoviePlayerAppDelegate.h')

        and:
        def tmpMFile = new File(tmpDir, mFile.name)
        tmpMFile.text = mFile.text
        def tmpHFile = new File(tmpDir, hFile.name)
        tmpHFile.text = hFile.text

        and:
        def keyToAdd = 'TESTKET'

        when:
        sourceHelper.addApphanceInit(tmpDir, keyToAdd)

        then:
        tmpMFile.text.contains(keyToAdd)
        tmpDir.deleteDir()

        where:
        baseDir << [
                'src/test/resources/com/apphance/ameba/plugins/ios/apphance/bracketInLine',
                'src/test/resources/com/apphance/ameba/plugins/ios/apphance/bracketNotInLine'
        ]
    }
}
