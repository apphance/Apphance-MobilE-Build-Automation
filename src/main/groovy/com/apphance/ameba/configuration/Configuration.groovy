package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.AbstractProperty
import com.google.inject.Inject

import java.lang.reflect.Field

import static org.apache.commons.lang.StringUtils.join

abstract class Configuration {

    @Inject
    @groovy.transform.PackageScope
    PropertyPersister propertyPersister

    def init() {
        //TODO add whole configuration enabling (android.apphance.enabled...)
        amebaProperties.each {
            it.value = propertyPersister.get(it.name)
        }

        String enabled = propertyPersister.get(nameKey)
        def enabledValue = Boolean.valueOf(enabled)
        if (enabled && enabledValue != this.enabled) {
            this.enabled = enabledValue
        }
    }

    abstract boolean isEnabled()

    void setEnabled(boolean enabled) {
        throw new IllegalStateException("Cannot change '$configurationName' enabled status to: $enabled")
    }

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

    Collection<? extends Configuration> getSubConfigurations() {
        []
    }

    String getNameKey() {
        configurationName.replace(' ', '.').toLowerCase()
    }
}