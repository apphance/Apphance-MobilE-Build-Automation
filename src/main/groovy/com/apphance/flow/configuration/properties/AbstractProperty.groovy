package com.apphance.flow.configuration.properties

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

    Closure<String> doc = { message ?: 'default doc' } //TODO: (Closure<String>) { throw new GradleException("Property $name has empty doc field!") }

    abstract void setValue(String value)

    T getValue() {
        value
    }

    void resetValue() {
        this.@value = null
    }

    @Override
    String toString() { "$name = ${getValue()}" }
}