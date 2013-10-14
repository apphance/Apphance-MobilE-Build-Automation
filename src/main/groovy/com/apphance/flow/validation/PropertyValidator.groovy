package com.apphance.flow.validation

import com.apphance.flow.configuration.properties.AbstractProperty

import static org.apache.commons.lang.StringUtils.isNotEmpty

class PropertyValidator {

    List<String> validateProperties(AbstractProperty... properties) {
        properties?.collect {
            validateCondition(it.validator(it.value), "Incorrect value $it.value of $it.name property")
        }?.findAll { isNotEmpty(it) }
    }

    String validateCondition(Boolean condition, String message) {
        condition ? '' : message
    }

    boolean throwsException(Closure c) {
        try { c.call() } catch (Exception e) { return true }
        false
    }
}
