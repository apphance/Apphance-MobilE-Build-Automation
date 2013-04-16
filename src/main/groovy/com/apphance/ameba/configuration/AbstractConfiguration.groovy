package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.AbstractProperty
import com.google.inject.Inject

import java.lang.reflect.Field

import static org.apache.commons.lang.StringUtils.join

abstract class AbstractConfiguration implements Configuration {

    public static final String ACCESS_DENIED = 'Access denied to property. Configuration disabled.'

    @Inject
    PropertyPersister propertyPersister

    @Inject
    def init() {
        amebaProperties.each {
            it.value = propertyPersister.get(it.name)
        }

        String enabled = propertyPersister.get(enabledPropKey)
        def enabledValue = Boolean.valueOf(enabled)
        if (enabled && enabledValue != this.enabled) {
            this.enabled = enabledValue
        }
    }

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

    abstract boolean isActive()

    @Override
    public String toString() {
        "Configuration $configurationName: ${join(amebaProperties, '\n')}\n";
    }

    Collection<? extends AbstractConfiguration> getSubConfigurations() {
        []
    }

    String getEnabledPropKey() {
        configurationName.replace(' ', '.').toLowerCase() + '.enabled'
    }
}