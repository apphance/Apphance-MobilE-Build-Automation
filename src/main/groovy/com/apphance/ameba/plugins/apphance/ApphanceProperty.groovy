package com.apphance.ameba.plugins.apphance

/**
 * Properties for Apphance integration.
 *
 */
enum ApphanceProperty {

    APPLICATION_KEY("apphance.appkey", "Application key in Apphance"),
    APPHANCE_MODE("apphance.mode", "Apphance mode", "QA"),
    APPHANCE_LOG_EVENTS("apphance.log.events", "(experimental) Log events from standard widgets (one of [true,false]", "false"),
    APPHANCE_LIB("apphance.lib", "Library version to be used with apphance (groovy style dependency)")

    final String propertyName
    final String description
    final String defaultValue

    ApphanceProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }

    public static final String DESCRIPTION = 'Apphance properties'

}
