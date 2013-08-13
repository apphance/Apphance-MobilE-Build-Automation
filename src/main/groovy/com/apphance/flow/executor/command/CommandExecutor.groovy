package com.apphance.flow.executor.command

import com.apphance.flow.executor.linker.FileLinker
import com.apphance.flow.util.FlowUtils
import com.apphance.flow.util.Preconditions
import org.gradle.api.logging.Logging

import javax.inject.Inject

import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.flow.executor.command.CommandLogFilesGenerator.LogFile.STD

@Mixin([Preconditions, FlowUtils])
class CommandExecutor {

    def logger = Logging.getLogger(getClass())

    private FileLinker fileLinker
    private CommandLogFilesGenerator logFileGenerator

    @Inject
    CommandExecutor(FileLinker fileLinker, CommandLogFilesGenerator logFileGenerator) {
        this.fileLinker = fileLinker
        this.logFileGenerator = logFileGenerator
    }

    Iterator<String> executeCommand(Command c) {

        def commandLogs = logFileGenerator.commandLogFiles()

        logger.lifecycle("Executing command: '${c.commandForPublic}', in dir: '${c.runDir}'")
        logger.lifecycle("Command std: ${fileLinker.fileLink(commandLogs[STD])}")
        logger.lifecycle("Command err: ${fileLinker.fileLink(commandLogs[ERR])}")

        Process process = runCommand(c, commandLogs)

        Integer exitValue = process?.waitFor()
        handleProcessResult exitValue, c, commandLogs[STD], commandLogs[ERR]

        commandLogs[STD]?.newInputStream()?.newReader()?.iterator()
    }

    private Process runCommand(Command command, Map<LogFile, File> commandLog) {
        try {
            def processBuilder = new ProcessBuilder(command.commandForExecution)
            processBuilder.
                    directory(command.runDir).
                    environment().putAll(command.environment)

            //out and err is redirected separately because xcodebuild for some commands returns '0' but display
            //warnings which are redirected to std, then parsing of output command fails
            //separate stream redirection solves this issue
            if (commandLog[STD]) processBuilder.redirectOutput(commandLog[STD])
            if (commandLog[ERR]) processBuilder.redirectError(commandLog[ERR])

            processBuilder.start()

        } catch (Exception e) {
            if (command.failOnError) {
                throw new CommandFailedException(e.message, command, commandLog[STD], commandLog[ERR])
            }
            logger.error("Error while executing command: ${command.commandForPublic}, in dir: ${command.runDir}, error: ${e.message}")
            null
        }
    }

    void handleProcessResult(Integer exitValue, Command command, File stdoutLog, File stderrLog) {
        if (exitValue != 0) {
            if (command.failOnError) {
                throw new CommandFailedException("Error while executing: '$command.commandForPublic', in dir: '$command.runDir', exit value: '$exitValue'.",
                        command, stdoutLog, stderrLog)
            }
            logger.warn("Executor is set not to fail on error, but command exited with value not equal to '0': '$exitValue'." +
                    " Might be potential problem, investigate error log: ${fileLinker.fileLink(stderrLog)}")
        }

        if (stderrLog?.size()) {
            logger.warn("Command err: ${fileLinker.fileLink(stderrLog)}, contains some text. It may be info about potential problems")
        }
    }
}
