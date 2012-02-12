package com.apphance.ameba.ios.plugins.fonemonkey
public enum IOSFoneMonkeyProperty {
    FONE_MONKEY_CONFIGURATION(true, 'ios.fonemonkey.configuration', 'FoneMonkey build configuration', 'Debug'),

    public static final String DESCRIPTION = 'iOS FoneMonkey properties'

    final boolean optional
    final String propertyName
    final String description
    final String defaultValue

    IOSFoneMonkeyProperty(boolean optional, String propertyName, String description, String defaultValue) {
        this.optional = optional
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
