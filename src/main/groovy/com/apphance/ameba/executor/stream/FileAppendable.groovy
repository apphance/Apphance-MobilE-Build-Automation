package com.apphance.ameba.executor.stream

import com.apphance.ameba.util.Preconditions

@Mixin(Preconditions)
class FileAppendable implements StringAppendable {

    private File outputFile

    FileAppendable(File outputFile) {
        this.outputFile = outputFile

        try {outputFile.createNewFile()} catch (e){}

        // validate file
        validate(outputFile.canWrite()) {
                throw new IllegalArgumentException("Can not write to file: ${outputFile.absolutePath}")
        }
    }

    @Override
    void append(String message) {
        outputFile << message
    }
}
