package com.apphance.ameba.ios.plugins.fonemonkey
public enum IOSFoneMonkeyProperty {
    FONE_MONKEY_CONFIGURATION(true, 'ios.fonemonkey.configuration', 'FoneMonkey build configuration'),

    final boolean optional
    final String propertyName
    final String description

    public static final String DESCRIPTION = 'iOS FoneMonkey properties'

    IOSFoneMonkeyProperty(boolean optional, String propertyName, String description) {
        this.optional = optional
        this.propertyName = propertyName
        this.description = description
    }
}
