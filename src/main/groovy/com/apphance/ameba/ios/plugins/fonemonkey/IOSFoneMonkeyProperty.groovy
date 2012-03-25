package com.apphance.ameba.ios.plugins.fonemonkey
/**
 * Properties for Fone Monkey tests.
 *
 */
public enum IOSFoneMonkeyProperty {
    FONE_MONKEY_CONFIGURATION('ios.fonemonkey.configuration', 'FoneMonkey build configuration', 'Debug'),

    public static final String DESCRIPTION = 'iOS FoneMonkey properties'

    final String propertyName
    final String description
    final String defaultValue

    IOSFoneMonkeyProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
