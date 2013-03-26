package com.apphance.ameba.configuration

import java.lang.reflect.Field

abstract class Configuration {

    abstract boolean isEnabled()

    abstract void setEnabled(boolean enabled)

    abstract int getOrder()

    List<Field> getAmebaProperties() {
        getClass().declaredFields.findAll {
            it.accessible = true
            it.get(this)?.class == Prop
        }
    }

    abstract String getConfigurationName()

}