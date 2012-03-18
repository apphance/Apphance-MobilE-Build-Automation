package com.apphance.ameba.android.plugins.apphance

enum AndroidApphanceProperty {

    APPLICATION_KEY("apphance.appkey", "Application key in Apphance"),
    APPHANCE_MODE("apphance.mode", "Apphance mode (one of [QA,Silent]", "QA"),
    APPHANCE_LOG_EVENTS("apphance.log.events", "Log events from standard widgets (one of [true,false]", "false"),

    public static final DESCRIPTION = 'Apphance properties'
    final String propertyName
    final String description
    final String defaultValue

    AndroidApphanceProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
