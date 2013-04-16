package com.apphance.ameba.configuration.properties

abstract class AbstractProperty<T> {

    String name
    String message

    protected T value

    Closure<T> defaultValue = { null as T }
    Closure<List<String>> possibleValues

    Closure<Boolean> validator = { true }

    Closure<Boolean> askUser = { true }

    Closure<String> persistentForm = { value?.toString() ?: '' }

    abstract void setValue(String value);

    T getValue() {
        value
    }

    @Override
    String toString() { "$name = $value" }
}