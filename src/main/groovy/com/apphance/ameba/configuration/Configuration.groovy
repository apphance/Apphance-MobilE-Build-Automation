package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.AbstractProperty

import java.lang.reflect.Field

abstract class Configuration {

    abstract boolean isEnabled()

    abstract void setEnabled(boolean enabled)

    abstract int getOrder()

    List<Field> getPropertyFields() {
        getClass().declaredFields.findAll {
            it.accessible = true
            it.get(this)?.class?.superclass == AbstractProperty
        }
    }

    List<AbstractProperty> getAmebaProperties() {
        propertyFields*.get(this)
    }

    abstract String getConfigurationName()

}