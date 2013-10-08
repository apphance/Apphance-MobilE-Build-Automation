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

    def 'obtains relative path'() {
        expect:
        FileManager.relativeTo(root.absolutePath, path.absolutePath).path == expected

        where:
        root                                           | path                                           | expected
        new File('projects/test/android')               | new File('projects/test/android/android-basic') | 'android-basic'
        new File('projects/test/android/android-basic') | new File('projects/test/android')               | '..'
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
