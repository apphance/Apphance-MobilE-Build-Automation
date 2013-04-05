package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.AbstractProperty
import com.google.inject.Inject

import java.lang.reflect.Field

import static org.apache.commons.lang.StringUtils.join

abstract class Configuration {

    @Inject PropertyPersister propertyPersister

    def init() {
        //TODO add whole configuration enabling (android.apphance.enabled...)
        amebaProperties.each {
            it.value = propertyPersister.get(it.name)
        }
    }

    abstract boolean isEnabled()

    abstract void setEnabled(boolean enabled)

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

    @Override
    public String toString() {
        "Configuration $configurationName: ${join(amebaProperties, '\n')}\n";
    }
}