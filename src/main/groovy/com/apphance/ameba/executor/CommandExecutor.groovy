package com.apphance.ameba.executor

import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.executor.log.CommandLogFilesGenerator
import com.apphance.ameba.util.Preconditions
import org.gradle.api.logging.Logging

import javax.inject.Inject

import static com.apphance.ameba.executor.log.CommandLogFilesGenerator.LogFile
import static com.apphance.ameba.executor.log.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.log.CommandLogFilesGenerator.LogFile.STD
import static java.lang.System.getProperties

@Mixin(Preconditions)
class CommandExecutor {

    def l = Logging.getLogger(getClass())

    private FileLinker fileLinker
    private CommandLogFilesGenerator logFileGenerator

    @Inject
    CommandExecutor(FileLinker fileLinker, CommandLogFilesGenerator logFileGenerator) {
        this.fileLinker = fileLinker
        this.logFileGenerator = logFileGenerator
    }

    Process startCommand(Command c) {

        def commandLogs = logFileGenerator.commandLogFiles()

        l.lifecycle("Starting command: '${c.commandForPublic}', in dir: '${c.runDir}' in background")
        l.lifecycle("Command std: ${fileLinker.fileLink(commandLogs[STD])}")
        l.lifecycle("Command err: ${fileLinker.fileLink(commandLogs[ERR])}")

        Process process = runCommand(c, commandLogs)

        process
    }

    List<String> executeCommand(Command c) {

        def commandLogs = logFileGenerator.commandLogFiles()

        l.lifecycle("Executing command: '${c.commandForPublic}', in dir: '${c.runDir}'")
        l.lifecycle("Command std: ${fileLinker.fileLink(commandLogs[STD])}")
        l.lifecycle("Command err: ${fileLinker.fileLink(commandLogs[ERR])}")

        Process process = runCommand(c, commandLogs)

        Integer exitValue = process?.waitFor()

        handleExitValue(exitValue, c)

        if (commandLogs[ERR].text) {
            l.warn("Command err: ${fileLinker.fileLink(commandLogs[ERR])}, contains some text. It may be info about" +
                    " potential problems")
        }

        commandLogs[STD].readLines()
    }

    private Process runCommand(Command c, Map<LogFile, File> commandLog) {
        Process process = null

        try {
            def processBuilder = new ProcessBuilder(c.commandForExecution)
            //out and err is redirected separately because xcodebuild for some commands returns '0' but display
            //warnings which are redirected to std, then parsing of output command fails
            //separate stream redirection solves this issue
            processBuilder.
                    directory(c.runDir).
                    redirectInput(prepareInputFile(c.input)).
                    redirectOutput(commandLog[STD]).
                    redirectError(commandLog[ERR]).
                    environment().putAll(c.environment)

            process = processBuilder.start()

        } catch (Exception e) {
            if (c.failOnError)
                throw new CommandFailedException(e.message, c)
        }

        process
    }

    private File prepareInputFile(Collection<String> input) {
        def inputFile = new File(properties['java.io.tmpdir'].toString(), 'cmd-input')
        inputFile.delete()
        inputFile.createNewFile()//empty file is returned if no input passed
        if (input) {
            input.each { inputFile << "${it.trim()}\n" }
        }
        inputFile
    }

    private void handleExitValue(Integer exitValue, Command c) {
        throwIfCondition(
                (exitValue != 0 && c.failOnError),
                "Error while executing: '${c.commandForPublic}', in dir: '${c.runDir}', " +
                        "exit value: '${exitValue}'"
        )
        if (exitValue != 0 && !c.failOnError) {
            l.warn("Executor is set not to fail on error, but command exited with value not equal to '0': '$exitValue'." +
                    " Might be potential problem, investigate error logs")
        }
    }
}
