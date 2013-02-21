package com.apphance.ameba.executor

import com.apphance.ameba.util.Preconditions
import com.apphance.ameba.util.file.FileSystemOutput
import org.gradle.api.logging.Logging

import static com.apphance.ameba.util.file.FileManager.mkdir
import static java.lang.System.err

@Mixin(Preconditions)
class CommandExecutor {

    def l = Logging.getLogger(getClass())



    Process startCommand(Command c) {
        l.warn("Following 'Command' properties are not handled in background mode: 'failOnError', 'retry', 'silent'")
        l.lifecycle("Executing command: [${displayableCmd(c)}], in dir: ${c.runDir} in background")

        def stdOut = new FileSystemOutput(c.output)
        def stdErr = new FileSystemOutput(c.output, err)

        Process process = runCommand(c)

        process.consumeProcessErrorStream(stdErr)
        process.consumeProcessOutputStream(stdOut)

        process
    }

    Collection<String> executeCommand(Command c) {

        l.warn("Following 'Command' properties are not handled in background mode: 'output'")

        mkdir(c.project.file('log'))

        FileSystemOutput stdOut = standardOutput()
        FileSystemOutput errOut = standradError()


        l.lifecycle("Executing command: [${displayableCmd(c)}], in dir: '${c.runDir}'")

        Process process = runCommand(c)

        Thread stdOutThread = process.consumeProcessOutputStream(stdOut)
        Thread stdErrThread = process.consumeProcessErrorStream(errOut)

        int exitValue = waitForProcess(process, stdOutThread, stdErrThread)

        handleExitValue(exitValue, c, errOut)

        stdOut.sb.toString().split('\n')
    }



    //TODO
    private FileSystemOutput standardOutput() {
        new FileSystemOutput(null)
    }

    //TODO
    private FileSystemOutput standradError() {
        new FileSystemOutput(null)
    }

    private Process runCommand(Command c) {
        Process process

        try {
            process = escapeCommand(c).execute(c.envp, c.runDir)
        } catch (Exception e) {
            throw new CommandFailedException(e.message, c)
        }

        handleProcessInput(process, c.input)
        process
    }

    private String[] escapeCommand(Command c) {
        c.cmd.collect { it.startsWith(c.escapePrefix) ? it.replaceFirst(c.escapePrefix, '') : it }
    }

    private int waitForProcess(Process process, Thread... toJoin) {
        def exitVal = process.waitFor()
        toJoin.each { it.join() }
        exitVal
    }

    private void handleProcessInput(Process process, Collection<String> input) {
        if (!input) return
        process.withWriter { w -> input.each { w << it } }
    }

    private void handleExitValue(int exitValue, Command c, FileSystemOutput errorOutput) {
        throwIfCondition(
                (exitValue != 0 && c.failOnError),
                "Error while executing: '${displayableCmd(c)}', in dir: '${c.runDir}', " +
                        "exit value: '${exitValue}', error output ${errorOutput.sb.toString()}."
        )
    }

    private String displayableCmd(Command c) {
        def sb = new StringBuilder()
        c.cmd.each {
            def s = it.startsWith(c.escapePrefix) ?
                '*' * it.replaceFirst(c.escapePrefix, '').length()
            :
                it
            sb.append(s).append(' ')
        }
        sb.toString().trim()
    }
}
