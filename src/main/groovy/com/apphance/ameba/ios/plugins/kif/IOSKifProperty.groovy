package com.apphance.ameba.ios.plugins.kif
public enum IOSKifProperty {
    KIF_CONFIGURATION('ios.kif.configuration', 'KIF build configuration', 'Debug');

    public static final String DESCRIPTION = 'iOS KIF properties'

    final String propertyName
    final String description
    final String defaultValue

    IOSKifProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
