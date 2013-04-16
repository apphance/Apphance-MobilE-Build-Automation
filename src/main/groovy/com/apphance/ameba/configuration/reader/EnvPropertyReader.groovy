package com.apphance.ameba.configuration.reader

import com.apphance.ameba.util.Preconditions

@Mixin(Preconditions)
class EnvPropertyReader {

    String readProperty(String name) {
        validate(name != null && !name.trim().empty, {
            throw new IllegalArgumentException('Null or empty property name passed')
        })

        String property = null

        for (Closure<String> c in [{ System.getProperty(it) }, { System.getenv(it.toUpperCase().replace('.', '_')) }]) {
            property = c.call(name)
            if (property)
                break
        }

        property
    }
}
