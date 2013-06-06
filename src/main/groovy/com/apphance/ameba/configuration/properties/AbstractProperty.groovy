package com.apphance.ameba.configuration.properties

abstract class AbstractProperty<T> {

    String name
    String message
    public String validationMessage = ''

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

    @Override
    String toString() { "$name = ${getValue()}" }

    /**
     * Default value of property used in configuration wizard calculated from <code>value</code>, <code>defaultValue</code> and <code>possibleValues</code>
     */
    String effectiveDefaultValue() {
        getValue() ?: defaultValue() ?: possibleValues() ? possibleValues().get(0) : ''
    }

    String getFailedValidationMessage() {
        "Validation failed for property: $name $validationMessage"
    }
}