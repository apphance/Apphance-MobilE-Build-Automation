package com.apphance.flow.configuration.properties

import org.gradle.api.GradleException

abstract class AbstractProperty<T> {

    String name
    String message
    String validationMessage = ''
    Boolean initialized = false

    protected T value

    Closure<T> defaultValue = { null as T }

    Closure<List<String>> possibleValues = { [] as List<String> }

    Closure<Boolean> validator = { possibleValues().empty || it in possibleValues() }

    Closure<Boolean> interactive = { true }

    Closure<String> persistentForm = { value?.toString() ?: '' }

    Closure<Boolean> required = { false }

    abstract void setValue(String value);

    T getValue() {
        value
    }

    void resetValue() {
        this.@value = null
    }

    @Override
    String toString() { "$name = ${getValue()}" }

    String getFailedValidationMessage() {
        "Validation failed for property: $name $validationMessage"
    }

    T getNotEmptyValue() {
        if (value) return getValue()
        else throw new GradleException("Invalid $message. property name: $name")
    }
}