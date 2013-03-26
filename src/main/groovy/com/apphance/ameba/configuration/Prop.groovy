package com.apphance.ameba.configuration

import groovy.transform.ToString

@ToString
class Prop<T> {

    String name
    String message
    Closure<T> defaultValue = { null }
    T value
    Closure<Boolean> validator = { true }
    List possibleValues

}
