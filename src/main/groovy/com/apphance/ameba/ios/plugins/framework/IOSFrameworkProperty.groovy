package com.apphance.ameba.ios.plugins.framework
public enum IOSFrameworkProperty {
    FRAMEWORK_TARGET(false, 'ios.framework.target', 'Target to build framework project with'),
    FRAMEWORK_CONFIGURATION(false, 'ios.framework.configuration', 'Configuration to build framework project with'),
    FRAMEWORK_VERSION(true, 'ios.framework.version', 'Version of framework (usually single alphabet letter A)','A'),
    FRAMEWORK_HEADERS(true, 'ios.framework.headers', 'List of headers (coma separated) that should be copied to the framework'),
    FRAMEWORK_RESOURCES(true, 'ios.framework.resources', 'List of resources (coma separated) that should be copied to the framework'),

    public static final String DESCRIPTION = 'iOS Framework properties'

    final boolean optional
    final String propertyName
    final String description
    final String defaultValue

    IOSFrameworkProperty(boolean optional, String propertyName, String description, String defaultValue = null) {
        this.optional = optional
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
