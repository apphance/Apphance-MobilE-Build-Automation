package com.apphance.ameba.ios.plugins.kif
/**
 * Properties for IOS KIF framework.
 *
 */
public enum KifProperty {
    KIF_CONFIGURATION('ios.kif.configuration', 'KIF build configuration', 'Debug'),

    public static final String DESCRIPTION = 'iOS KIF properties'

    final String propertyName
    final String description
    final String defaultValue

    KifProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
