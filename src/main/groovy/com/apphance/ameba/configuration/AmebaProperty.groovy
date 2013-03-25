package com.apphance.ameba.configuration

import groovy.transform.ToString

@ToString
class AmebaProperty {
    String name
    String message
    Closure defaultValue = { '' }
    String value
    Closure validator = { true }
    List possibleValues
}
