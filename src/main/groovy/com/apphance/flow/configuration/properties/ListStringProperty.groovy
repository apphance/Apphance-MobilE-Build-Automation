package com.apphance.flow.configuration.properties

import static org.apache.commons.lang.StringUtils.isNotBlank

class ListStringProperty extends AbstractProperty<List<String>> {

    private static String SEPARATOR = ','

    @Override
    void setValue(String value) {
        if (value?.trim()) {
            this.@value = convert(value)
        }
    }

    private List<String> convert(String value) {
        value.trim().replaceAll('[\\[\\]]', '').split(SEPARATOR)*.trim().findAll { isNotBlank(it) }
    }

    Closure<Boolean> validator = { possibleValues().empty || possibleValues().containsAll(convert(it)) }

    Closure<String> persistentForm = { value?.join(SEPARATOR) ?: '' }

    static String getSEPARATOR() {
        SEPARATOR
    }

}