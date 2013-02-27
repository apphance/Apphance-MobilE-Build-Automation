package com.apphance.ameba.executor.log

import com.apphance.ameba.util.Preconditions

import javax.inject.Inject

@Mixin(Preconditions)
class CommandLogFileGenerator {

    private int fileCounter = 0
    private String logDir

    @Inject
    CommandLogFileGenerator(String logDir) {
        this.logDir = logDir
    }

    private String nextFilename() {
        "command-${fileCounter++}-output.log"
    }

    public File commandLogFile() {

        def logFile = new File(logDir, nextFilename())

        try { logFile.createNewFile() } catch (e) {}

        validate(logFile.canWrite()) {
            throw new IllegalArgumentException("Can not write to file: ${logFile.absolutePath}")
        }

        logFile
    }

}
