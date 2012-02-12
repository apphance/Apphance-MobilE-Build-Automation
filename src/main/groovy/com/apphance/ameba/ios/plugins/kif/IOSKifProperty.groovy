package com.apphance.ameba.ios.plugins.kif
public enum IOSKifProperty {
    KIF_CONFIGURATION(true, 'ios.kif.configuration', 'KIF build configuration', 'Debug');

    public static final String DESCRIPTION = 'iOS KIF properties'

    final boolean optional
    final String propertyName
    final String description
    final String defaultValue

    IOSKifProperty(boolean optional, String propertyName, String description, String defaultValue) {
        this.optional = optional
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
