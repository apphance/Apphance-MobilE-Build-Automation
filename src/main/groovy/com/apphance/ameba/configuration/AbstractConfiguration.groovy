package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.AbstractProperty
import com.apphance.ameba.configuration.reader.PropertyPersister

import javax.inject.Inject
import java.lang.reflect.Field

import static org.apache.commons.lang.StringUtils.join

abstract class AbstractConfiguration implements Configuration {

    @Inject
    PropertyPersister propertyPersister

    List<String> errors = []

    @Inject
    void init() {
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
        List<Field> fields = []
        def findDeclaredFields
        findDeclaredFields = { Class c ->
            fields.addAll(c.declaredFields)
            if (c.superclass)
                findDeclaredFields(c.superclass)
        }
        findDeclaredFields(getClass())
        fields.findAll {
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
        "Configuration '$configurationName'" + (amebaProperties ? " \n${join(amebaProperties, '\n')}\n" : '');
    }

    Collection<? extends AbstractConfiguration> getSubConfigurations() {
        []
    }

    String getEnabledPropKey() {
        configurationName.replace(' ', '.').toLowerCase() + '.enabled'
    }

    final def check(condition, String message) {
        if (!condition) {
            errors << message
        }
    }

    final List<String> verify() {
        checkProperties()
        errors
    }

    protected String checkException(Closure cl) {
        try {
            cl.call()
        } catch (e) {
            return e.message
        }
        ''
    }

    void checkProperties() {}

    @Override
    boolean canBeEnabled() {
        return true
    }

    @Override
    String getMessage() {
        ""
    }
}