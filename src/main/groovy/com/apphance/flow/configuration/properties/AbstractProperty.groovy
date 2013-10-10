package com.apphance.flow.configuration.properties

import org.gradle.api.GradleException

abstract class AbstractProperty<T> {

    String name
    String message
    String validationMessage = ''

    protected T value = null

    Closure<T> defaultValue = { null as T }

    Closure<List<String>> possibleValues = { [] as List<String> }

    Closure<Boolean> validator = { possibleValues().empty || it in possibleValues() }

    Closure<Boolean> interactive = { true }

    Closure<String> persistentForm = { value?.toString() ?: '' }

    Closure<Boolean> required = { false }

    Closure<String> doc = { message ?: { throw new GradleException("Property $name has empty doc field!") }() }

    abstract void setValue(String value)

    T getValue() {
        value
    }

    boolean hasValue() {
        value != null
    }

    void resetValue() {
        this.@value = null
    }

    @Override
    String toString() { "$name = ${getValue()}" }
}