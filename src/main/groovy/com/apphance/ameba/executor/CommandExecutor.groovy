package com.apphance.ameba.executor

import com.apphance.ameba.executor.linker.FileLinker
import com.apphance.ameba.util.Preconditions
import org.gradle.api.logging.Logging

import javax.inject.Inject

import static com.apphance.ameba.util.file.FileManager.mkdir

@Mixin(Preconditions)
class CommandExecutor {

    def l = Logging.getLogger(getClass())

    @Inject
    private FileLinker fileLinker

    Process startCommand(Command c) {
        mkdir(c.project.file('log'))

        def commandOutputFile = new File('log', 'logfile.txt')//TODO nazwa

        l.lifecycle("Executing command: [${displayableCmd(c)}], in dir: ${c.runDir} in background")
        l.lifecycle("Command output: ${fileLinker.fileLink()}")


        Process process = runCommand(c)

        process
    }

    Collection<String> executeCommand(Command c) {

        mkdir(c.project.file('log'))

        def commandOutputFile = new File('log', 'logfile.txt')//TODO nazwa

        l.lifecycle("Executing command: [${displayableCmd(c)}], in dir: '${c.runDir}'")
        l.lifecycle("Command output: ${fileLinker.fileLink()}")

        Process process = runCommand(c)

        int exitValue = process.waitFor()

        handleExitValue(exitValue, c)

        commandOutputFile.readLines()
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

    private void handleExitValue(int exitValue, Command c) {
        throwIfCondition(
                (exitValue != 0 && c.failOnError),
                "Error while executing: '${displayableCmd(c)}', in dir: '${c.runDir}', " +
                        "exit value: '${exitValue}'"
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
