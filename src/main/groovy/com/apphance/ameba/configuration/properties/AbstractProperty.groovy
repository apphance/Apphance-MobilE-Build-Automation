package com.apphance.ameba.configuration.properties

abstract class AbstractProperty<T> {

    String name
    String message

    protected T value

    Closure<T> defaultValue = { null as T }

    Closure<List<String>> possibleValues = { [] as List<String>}

    Closure<Boolean> validator = { possibleValues().empty || it in possibleValues() }

    Closure<Boolean> interactive = { true }

    Closure<String> persistentForm = { value?.toString() ?: '' }

    Closure<Boolean> required = { false }

    abstract void setValue(String value);

    T getValue() {
        value
    }

    @Override
    String toString() { "$name = $value" }
}