package com.apphance.ameba.executor.log

import spock.lang.Specification

import static java.lang.System.getProperties

class CommandLogFileGeneratorSpec extends Specification {

    def 'command log file generator returns correctFile'() {
        given:
        File logDir = new File(properties['java.io.tmpdir'].toString())

        and:
        def logFileGenerator = new CommandLogFileGenerator(logDir.canonicalPath)

        when:
        def nextFile = logFileGenerator.commandLogFile()

        then:
        nextFile.absolutePath == "${logDir.canonicalPath}${properties['file.separator']}command-0-output.log"
    }

    def 'command log file raises exception when can not write to file'() {
        given:
        File logDir = new File('/not/existing/dir')

        and:
        def logFileGenerator = new CommandLogFileGenerator(logDir.canonicalPath)

        when:
        logFileGenerator.commandLogFile()

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Can not write to file: /not/existing/dir/command-0-output.log"
    }
}
