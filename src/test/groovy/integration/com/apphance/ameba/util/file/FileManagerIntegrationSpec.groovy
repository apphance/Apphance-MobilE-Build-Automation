package com.apphance.ameba.util.file

import spock.lang.Specification

import static com.apphance.ameba.util.file.FileManager.findAllPackages
import static com.apphance.ameba.util.file.FileManager.removeMissingSymlinks

class FileManagerIntegrationSpec extends Specification {

    def 'removes symlinks'() {
        given:
        def dir = new File('.')
        and:
        def missingFileLink = 'missingFileLink'

        when:
        [
                'ln',
                '-s',
                'missingFile',
                missingFileLink,
        ].execute([], dir).waitFor()

        then:
        dir.list().contains(missingFileLink)
        !(new File(dir, missingFileLink).canonicalFile.exists())

        when:
        removeMissingSymlinks(dir)

        then:
        !(dir.list().contains(missingFileLink))
        !(new File(dir, missingFileLink).canonicalFile.exists())
    }

    def 'finds all packages correctly'() {

        given:
        def f = new File('src/test/groovy')
        def currentPackageList = []

        and:
        def packageList = [
                'com.apphance.ameba.integration.android',
                'com.apphance.ameba.integration.android.apphance',
                'com.apphance.ameba.integration.android.robolectric',
                'com.apphance.ameba.integration.android.robotium',
                'com.apphance.ameba.integration.android.setup',
                'com.apphance.ameba.integration.conventions',
                'com.apphance.ameba.integration.ios',
                'com.apphance.ameba.integration.ios.apphance',
                'com.apphance.ameba.integration.ios.setup',
                'com.apphance.ameba.unit.android',
                'com.apphance.ameba.unit.ios',
                'integration.com.apphance.ameba.util.file',
                'unit.com.apphance.ameba',
                'unit.com.apphance.ameba.detection',
                'unit.com.apphance.ameba.executor.command',
                'unit.com.apphance.ameba.executor.jython',
                'unit.com.apphance.ameba.executor.linker',
                'unit.com.apphance.ameba.plugins',
                'unit.com.apphance.ameba.plugins.release',
        ]

        when:
        findAllPackages('', f, currentPackageList)

        then:
        packageList == currentPackageList
    }
}
