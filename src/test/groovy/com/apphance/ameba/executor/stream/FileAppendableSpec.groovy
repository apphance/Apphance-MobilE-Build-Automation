package com.apphance.ameba.executor.stream

import spock.lang.Specification

class FileAppendableSpec extends Specification {

    def localId() { Thread.currentThread().id }

    def file = "file_appendable_${localId()}.log" as File

    def cleanup() {
        println file
        file.delete()
    }

    def 'file appendable writes to file'() {
        expect:
        !file.exists()

        when:
        def fileAppendable = new FileAppendable(file)
        fileAppendable.append('content')


        then:
        file.text == 'content'
    }

    def 'raises exception when can not write to not existing file'() {
        given:
        def file = new File('/no/such/path/file.log')

        when:
        new FileAppendable(file)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message =~ 'Can not write to file'
        exception.message =~ file.path
        exception.message =~ file.name
    }

    def 'raises exception when can not write to file'() {
        given:
        def file = Mock(File)
        file.canWrite() >> false

        when:
        new FileAppendable(file)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message =~ 'Can not write to file'
        exception.message =~ file.path
        exception.message =~ file.name
    }
}
