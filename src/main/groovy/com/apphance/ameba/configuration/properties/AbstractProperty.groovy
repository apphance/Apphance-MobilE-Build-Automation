package com.apphance.ameba.configuration.properties

abstract class AbstractProperty<T> {

    String name
    String message
    Closure<T> defaultValue = { null }
    protected T value
    Closure<Boolean> validator
    List<String> possibleValues

    abstract void setValue(String value);

    T getValue() {
        value
    }
}