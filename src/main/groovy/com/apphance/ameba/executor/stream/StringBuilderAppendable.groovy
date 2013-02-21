package com.apphance.ameba.executor.stream

import com.apphance.ameba.util.Preconditions

@Mixin(Preconditions)
class StringBuilderAppendable implements StringAppendable {

    private StringBuilder outputBuilder

    StringBuilderAppendable(StringBuilder outputBuilder) {
        this.outputBuilder = outputBuilder

        validate(outputBuilder != null) {
            throw new IllegalArgumentException('Null string builder passed')
        }
    }

    @Override
    void append(String message) {
        outputBuilder << message
    }
}
