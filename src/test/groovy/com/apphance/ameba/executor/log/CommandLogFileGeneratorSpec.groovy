package com.apphance.ameba.executor.log

import spock.lang.Specification

import static com.apphance.ameba.executor.log.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.log.CommandLogFilesGenerator.LogFile.STD
import static java.lang.System.getProperties

class CommandLogFileGeneratorSpec extends Specification {

    def 'command log file generator returns correctFile'() {
        given:
        File logDir = new File(properties['java.io.tmpdir'].toString())

        and:
        def logFileGenerator = new CommandLogFilesGenerator(logDir)

        when:
        def files = logFileGenerator.commandLogFiles()

        then:
        files[STD].absolutePath == "${logDir.absolutePath}${properties['file.separator']}command-0-out.log"
        files[ERR].absolutePath == "${logDir.absolutePath}${properties['file.separator']}command-0-err.log"
    }

    def 'command log file raises exception when can not write to file'() {
        given:
        File logDir = new File('/not/existing/dir')

        and:
        def logFileGenerator = new CommandLogFilesGenerator(logDir)

        when:
        logFileGenerator.commandLogFiles()

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Can not write to files: [STD=/not/existing/dir/command-0-out.log," +
                " ERR=/not/existing/dir/command-0-err.log]"
    }
}
