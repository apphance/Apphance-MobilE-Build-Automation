package com.apphance.flow.configuration

import com.apphance.flow.configuration.properties.AbstractProperty
import com.apphance.flow.configuration.reader.PropertyPersister

import javax.inject.Inject
import java.lang.reflect.Field

import static org.apache.commons.lang.StringUtils.join

abstract class AbstractConfiguration {

    @Inject PropertyPersister propertyPersister

    private List<String> errors = []

    @Inject
    void init() {
        flowProperties().each {
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

    abstract boolean isEnabled()

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

    List<AbstractProperty> flowProperties() {
        propertyFields*.get(this)
    }

    abstract String getConfigurationName()

    @Override
    public String toString() {
        "Configuration '$configurationName'" + (flowProperties() ? " \n${join(flowProperties(), '\n')}\n" : '');
    }

    Collection<? extends AbstractConfiguration> getSubConfigurations() {
        []
    }

    String getEnabledPropKey() {
        configurationName.replace(' ', '.').toLowerCase() + '.enabled'
    }

    final def check(condition, String message) {
        if (!condition) {
            this.@errors << message
        }
    }

    final List<String> verify() {
        checkProperties()
        this.@errors
    }

    void checkProperties() {}

    protected String checkException(Closure cl) {
        try {
            cl.call()
        } catch (e) {
            return e.message
        }
        ''
    }

    boolean canBeEnabled() {
        return true
    }

    String explainDisabled() {
        ""
    }

    def defaultValidation(AbstractProperty... properties) {
        properties.each {
            check it.validator(it.value), "Incorrect value $it.value of $it.name property"
        }
    }
}