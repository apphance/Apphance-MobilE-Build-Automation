package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.AbstractProperty
import com.google.inject.Inject

import java.lang.reflect.Field

import static org.apache.commons.lang.StringUtils.join

abstract class Configuration implements GroovyInterceptable {

    public static final String ACCESS_DENIED = 'Access denied to property. Configuration disabled.'
    @Inject
    @groovy.transform.PackageScope
    PropertyPersister propertyPersister

    def init() {
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

    def invokeMethod(String name, args) {
        if (name in ['isEnabled', 'getAmebaProperties', 'getPropertyFields', 'getClass'] || isEnabled() ||
                !(propertyFields*.name.collect { "(get|is)${it.capitalize()}" }.any{ name ==~ it })) {
            metaClass.getMetaMethod(name, args).invoke(this, args)
        } else {
            throw new IllegalStateException(ACCESS_DENIED)
        }
    }

    def getProperty(String name) {
        if (name in ['enabled', 'amebaProperties', 'propertyFields', 'class'] || isEnabled() || !(name in propertyFields*.name)) {
            metaClass.getMetaProperty(name).getProperty(this)
        } else {
            throw new IllegalStateException(ACCESS_DENIED)
        }
    }
}