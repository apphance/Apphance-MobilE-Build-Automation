package com.apphance.ameba.executor.log

import com.apphance.ameba.util.Preconditions

import javax.inject.Inject
import java.util.concurrent.atomic.AtomicInteger

import static com.apphance.ameba.executor.log.CommandLogFilesGenerator.LogFile.ERR
import static com.apphance.ameba.executor.log.CommandLogFilesGenerator.LogFile.STD

@Mixin(Preconditions)
class CommandLogFilesGenerator {

    private AtomicInteger fileCounter = new AtomicInteger()
    private File logDir

    @Inject
    CommandLogFilesGenerator(File logDir) {
        this.logDir = logDir
    }

    private Map<LogFile, String> nextFilenames() {
        def files = [
                (STD): "command-${fileCounter.get()}-out.log",
                (ERR): "command-${fileCounter.get()}-err.log"
        ]
        fileCounter.incrementAndGet()
        files
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
