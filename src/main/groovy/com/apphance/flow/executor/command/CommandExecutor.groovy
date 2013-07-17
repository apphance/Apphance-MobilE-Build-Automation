package com.apphance.flow.executor.command

import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.util.Preconditions
import org.gradle.api.logging.Logging

import javax.inject.Inject

import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD
import static java.lang.ProcessBuilder.Redirect.appendTo
import static java.lang.System.getProperties

@Mixin(Preconditions)
class CommandExecutor {

    def logger = Logging.getLogger(getClass())

    private FileLinker fileLinker
    private CommandLogFilesGenerator logFileGenerator

    @Inject
    CommandExecutor(FileLinker fileLinker, CommandLogFilesGenerator logFileGenerator) {
        this.fileLinker = fileLinker
        this.logFileGenerator = logFileGenerator
    }

    Process startCommand(Command c) {

        def commandLogs = logFileGenerator.commandLogFiles()

        logger.lifecycle("Starting command: '${c.commandForPublic}', in dir: '${c.runDir}' in background")
        logger.lifecycle("Command std: ${fileLinker.fileLink(commandLogs[STD])}")
        logger.lifecycle("Command err: ${fileLinker.fileLink(commandLogs[ERR])}")

        Process process = runCommand(c, commandLogs)

        process
    }

    Iterator<String> executeCommand(Command c) {

        def commandLogs = logFileGenerator.commandLogFiles()

        logger.lifecycle("Executing command: '${c.commandForPublic}', in dir: '${c.runDir}'")
        logger.lifecycle("Command std: ${fileLinker.fileLink(commandLogs[STD])}")
        logger.lifecycle("Command err: ${fileLinker.fileLink(commandLogs[ERR])}")

        Process process = runCommand(c, commandLogs)

        Integer exitValue = process?.waitFor()

        handleExitValue(exitValue, c)

        if (commandLogs[ERR]?.text) {
            logger.warn("Command err: ${fileLinker.fileLink(commandLogs[ERR])}, contains some text. It may be info about" +
                    " potential problems")
        }

        commandLogs[STD]?.newInputStream()?.newReader()?.iterator()
    }

    private Process runCommand(Command c, Map<CommandLogFilesGenerator.LogFile, File> commandLog) {
        Process process = null

        try {
            def processBuilder = new ProcessBuilder(c.commandForExecution)
            processBuilder.
                    directory(c.runDir).
                    redirectInput(prepareInputFile(c.input)).
                    environment().putAll(c.environment)

            //out and err is redirected separately because xcodebuild for some commands returns '0' but display
            //warnings which are redirected to std, then parsing of output command fails
            //separate stream redirection solves this issue
            if (commandLog[STD]) processBuilder.redirectOutput(commandLog[STD])
            if (commandLog[ERR]) processBuilder.redirectError(commandLog[ERR])

            process = processBuilder.start()

        } catch (Exception e) {
            if (c.failOnError) {
                throw new CommandFailedException(e.message, c)
            } else {
                logger.error("Error while executing command: ${c.commandForPublic}, in dir: ${c.runDir}, error: ${e.message}")
            }
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
        inputFile.deleteOnExit()
        inputFile
    }

    private void handleExitValue(Integer exitValue, Command c) {
        throwIfConditionTrue(
                (exitValue != 0 && c.failOnError),
                "Error while executing: '$c.commandForPublic', in dir: '$c.runDir', exit value: '$exitValue'"
        )
        if (exitValue != 0 && !c.failOnError) {
            logger.warn("Executor is set not to fail on error, but command exited with value not equal to '0': '$exitValue'." +
                    " Might be potential problem, investigate error logs")
        }
    }
}
