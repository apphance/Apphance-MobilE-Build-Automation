package com.apphance.flow.plugins.release.tasks

import spock.lang.Shared
import spock.lang.Specification

import static com.google.common.io.Files.createTempDir

class ImageNameFilterSpec extends Specification {

    @Shared
    def filter = new ImageNameFilter()
    private File file

    def 'image invalid when null or non-existing files passed'() {
        expect:
        !filter.isValid(file)

        where:
        file << [null, new File('file')]
    }

    def 'image invalid when no valid extension found'() {
        expect:
        def file = File.createTempFile('prefix', suffix)
        !filter.isValid(file)

        cleanup:
        file.delete()

        where:
        suffix << ['.psd', '.psp']
    }

    def 'image invalid when no valid prefix found'() {
        given:
        def tmpDir = createTempDir()

        expect:
        def rootDir = new File(tmpDir, dir)
        rootDir.mkdirs()
        def file = new File(rootDir, "prefix$suffix")
        file.createNewFile()
        !filter.isValid(file)

        cleanup:
        tmpDir.deleteDir()
        file.delete()

        where:
        dir        | suffix
        'build'    | '.jpg'
        'flow-ota' | '.gif'
    }

    def 'file is valid'() {
        given:
        def file = File.createTempFile('prefix', '.jpg')

        expect:
        filter.isValid(file)

        cleanup:
        file.delete()
    }
}
