package com.apphance.ameba.ios.plugins.kif
public enum IOSKifProperty {
    KIF_CONFIGURATION(true, 'ios.kif.configuration', 'KIF build configuration');

    final boolean optional
    final String propertyName
    final String description

    IOSKifProperty(boolean optional, String propertyName, String description) {
        this.optional = optional
        this.propertyName = propertyName
        this.description = description
    }
}
