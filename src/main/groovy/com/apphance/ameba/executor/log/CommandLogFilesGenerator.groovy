package com.apphance.ameba.executor.log

import com.apphance.ameba.util.Preconditions

import javax.inject.Inject
import java.util.concurrent.atomic.AtomicInteger

import static com.apphance.ameba.executor.log.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.log.CommandLogFilesGenerator.LogFile.STD

@Mixin(Preconditions)
//TODO singleton
class CommandLogFilesGenerator {

    private int fileCounter = 0
    private File logDir

    @Inject
    CommandLogFilesGenerator(File logDir) {
        this.logDir = logDir
    }

    private Map<LogFile, String> nextFilenames() {
        [
                (STD): "command-${fileCounter}-out.log",
                (ERR): "command-${fileCounter++}-err.log"//TODO
        ]
    }

    public Map<LogFile, File> commandLogFiles() {

        def logFiles = nextFilenames().collectEntries { it.value = new File(logDir, it.value); it }

        try { logFiles.each { it.value.createNewFile() } } catch (e) {}

        validate(logFiles.every { it.value.canWrite() }) {
            throw new IllegalArgumentException("Can not write to files: " +
                    "${logFiles.collect { it.value = it.value.absolutePath; it }}")
        }

        logFiles
    }

    static enum LogFile {
        STD, ERR
    }
}
