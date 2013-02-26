package com.apphance.ameba.executor

import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.executor.log.CommandLogFileGenerator
import com.apphance.ameba.util.Preconditions
import org.gradle.api.logging.Logging

import javax.inject.Inject

import static java.lang.System.getProperties

@Mixin(Preconditions)
class CommandExecutor {

    def l = Logging.getLogger(getClass())

    @Inject
    private FileLinker fileLinker
    @Inject
    private CommandLogFileGenerator logFileGenerator

    Process startCommand(Command c) {

        def commandLog = logFileGenerator.commandLogFile()

        l.lifecycle("Starting command: '${c.commandForPublic}', in dir: '${c.runDir}' in background")
        l.lifecycle("Command output: ${fileLinker.fileLink(commandLog)}")

        Process process = runCommand(c, commandLog)

        process
    }

    Collection<String> executeCommand(Command c) {

        def commandLog = logFileGenerator.commandLogFile()

        l.lifecycle("Executing command: '${c.commandForPublic}', in dir: '${c.runDir}'")
        l.lifecycle("Command output: ${fileLinker.fileLink(commandLog)}")

        Process process = runCommand(c, commandLog)

        Integer exitValue = process?.waitFor()

        handleExitValue(exitValue, c)

        commandLog.readLines()
    }

    private Process runCommand(Command c, File commandLog) {
        Process process = null

        try {
            def processBuilder = new ProcessBuilder(c.commandForExecution)
            processBuilder.
                    directory(c.runDir).
                    redirectInput(prepareInputFile(c.input)).
                    redirectOutput(commandLog).
                    redirectErrorStream(true).
                    redirectError(commandLog).
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
        if (!input) {
            input.each { inputFile << "$it\n" }
        }
        inputFile
    }

    private void handleExitValue(Integer exitValue, Command c) {
        throwIfCondition(
                (exitValue != 0 && c.failOnError),
                "Error while executing: '${c.commandForPublic}', in dir: '${c.runDir}', " +
                        "exit value: '${exitValue}'"
        )
    }
}
