package com.apphance.flow.configuration.reader

import com.apphance.flow.util.Preconditions

@Mixin(Preconditions)
class PropertyReader {

    String systemProperty(String name) {
        verify(name)
        System.getProperty(name)
    }

    String envVariable(String name) {
        verify(name)
        System.getenv(name) ?: null
    }

    private void verify(String name) {
        validate(name != null && !name.trim().empty, {
            throw new IllegalArgumentException('Null or empty property name passed')
        })
    }
}
