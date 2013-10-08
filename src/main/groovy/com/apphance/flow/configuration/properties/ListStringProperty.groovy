package com.apphance.flow.configuration.properties

import static org.apache.commons.lang.StringUtils.isNotBlank
import static org.gradle.api.logging.Logging.getLogger

class ListStringProperty extends AbstractProperty<List<String>> {

    public static final String SEPARATOR = ','
    private logger = getLogger(getClass())

    @Override
    void setValue(String value) {
        if (value?.trim()) {
            this.@value = convert(value)
        }
    }

    List<String> convert(String value) {
        value.trim().replaceAll('[\\[\\]]', '').split(SEPARATOR)*.trim().findAll { isNotBlank(it) }
    }

    Closure<Boolean> validator = { possibleValues().empty || possibleValues().containsAll(convert(it)) }

    Closure<String> persistentForm = { value?.join(SEPARATOR) ?: '' }

    void makeUnique() {
        if (value?.unique(false) != value) {
            logger.warn("'$name' property is not unique, making it unique")
            value.unique()
        }
    }
}