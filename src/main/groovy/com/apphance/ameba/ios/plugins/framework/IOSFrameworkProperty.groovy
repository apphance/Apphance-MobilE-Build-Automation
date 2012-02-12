package com.apphance.ameba.ios.plugins.framework
public enum IOSFrameworkProperty {
    FRAMEWORK_TARGET('ios.framework.target', 'Target to build framework project with'),
    FRAMEWORK_CONFIGURATION('ios.framework.configuration', 'Configuration to build framework project with'),
    FRAMEWORK_VERSION('ios.framework.version', 'Version of framework (usually single alphabet letter A)','A'),
    FRAMEWORK_HEADERS('ios.framework.headers', 'List of headers (coma separated) that should be copied to the framework'),
    FRAMEWORK_RESOURCES('ios.framework.resources', 'List of resources (coma separated) that should be copied to the framework'),

    public static final String DESCRIPTION = 'iOS Framework properties'

    final String propertyName
    final String description
    final String defaultValue

    IOSFrameworkProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
