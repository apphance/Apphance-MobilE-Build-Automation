package com.apphance.ameba.configuration

import java.lang.reflect.Field

abstract class Configuration {

    abstract boolean isEnabled()

    abstract void setEnabled(boolean enabled)

    abstract int getOrder()

    List<Field> getPropertyFields() {
        getClass().declaredFields.findAll {
            it.accessible = true
            it.get(this)?.class == Prop
        }
    }

    List<Prop> getAmebaProperties() {
        propertyFields*.get(this)
    }

    abstract String getConfigurationName()

}