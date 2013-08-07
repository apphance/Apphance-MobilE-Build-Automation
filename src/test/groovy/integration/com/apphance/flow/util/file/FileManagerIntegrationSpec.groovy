package com.apphance.flow.util.file

import com.apphance.flow.TestUtils
import com.apphance.flow.util.FlowUtils
import spock.lang.Ignore
import spock.lang.Specification

import static com.apphance.flow.util.file.FileManager.*

@Mixin([FlowUtils, TestUtils])
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

    @Ignore('this tests is ignored because the package structure of test dir is still under construction')
    def 'finds all packages correctly'() {

        given:
        def f = new File('src/test/groovy')
        def currentPackageList = []

        and:
        def packageList = [
                'com.apphance.flow.android',
                'com.apphance.flow.android.apphance',
                'com.apphance.flow.android.robolectric',
                'com.apphance.flow.android.robotium',
                'com.apphance.flow.android.setup',
                'com.apphance.flow.conventions',
                'com.apphance.flow.ios',
                'com.apphance.flow.ios.apphance',
                'com.apphance.flow.ios.setup',
                'com.apphance.flow.unit.android',
                'integration.com.apphance.flow',
                'integration.com.apphance.flow.util.file',
                'unit.com.apphance.flow',
                'unit.com.apphance.flow.detection',
                'unit.com.apphance.flow.executor.command',
                'unit.com.apphance.flow.executor.linker',
                'unit.com.apphance.flow.ios',
                'unit.com.apphance.flow.ios.plugins',
                'unit.com.apphance.flow.plugins.ios.apphance',
                'unit.com.apphance.flow.plugins.ios.buildplugin',
                'unit.com.apphance.flow.plugins.ios.test',
                'unit.com.apphance.flow.plugins', 'unit.com.apphance.flow.plugins.release',
        ]

        when:
        findAllPackages('', f, currentPackageList)

        then:
        packageList == currentPackageList
    }

    def 'obtains relative path'() {
        expect:
        FileManager.relativeTo(root.absolutePath, path.absolutePath).path == expected

        where:
        root                                           | path                                           | expected
        new File('testProjects/android')               | new File('testProjects/android/android-basic') | 'android-basic'
        new File('testProjects/android/android-basic') | new File('testProjects/android')               | '..'
    }

    def 'test relativeTo'() {
        given:
        def root = temporaryDir
        def lib = newDir root, 'libs/some/subdir'
        def other = newDir root, 'other'

        expect:
        relativeTo(root, lib) == 'libs/some/subdir'
        relativeTo(other, lib.parentFile) == '../libs/some'
        relativeTo(lib, other) == '../../../other'
    }

    def 'test replace'() {
        given:
        def file = newFile 'file.txt', "I like %placeholder% very much"

        when:
        replace(file, '%placeholder%', 'Spock')

        then:
        file.text == "I like Spock very much"
    }

    def 'test asProperties'() {
        given:
        def props = newFile 'file.properties', "pl.propA=valueA\ncom.propB=valueB"

        when:
        Properties properties = asProperties(props)

        then:
        properties.getProperty('pl.propA') == 'valueA'
        properties.getProperty('com.propB') == 'valueB'
        properties.getProperty('nonExisting') == null
    }

    def 'test isAndroidLibrary'() {
        expect:
        isAndroidLibrary(file.parentFile) == expected

        where:
        file                                                  | expected
        newFile('project.properties', '')                     | false
        newFile('project.properties', 'android.library=true') | true
    }
}
