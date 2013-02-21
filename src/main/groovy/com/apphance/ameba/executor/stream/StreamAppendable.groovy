package com.apphance.ameba.executor.stream

import com.apphance.ameba.util.Preconditions

@Mixin(Preconditions)
class StreamAppendable implements StringAppendable {
    private OutputStream outputStream

    StreamAppendable(OutputStream outputStream) {
        this.outputStream = outputStream

        validate(outputStream != null) {
            throw new IllegalArgumentException('Null output stream passed')
        }
    }

    @Override
    void append(String message) {
        outputStream << message
    }
}
