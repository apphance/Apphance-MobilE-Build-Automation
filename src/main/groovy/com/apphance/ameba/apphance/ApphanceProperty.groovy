package com.apphance.ameba.apphance

enum ApphanceProperty {

    APPLICATION_KEY("apphance.appkey", "Application key in Apphance"),
    APPHANCE_MODE("apphance.mode", "Apphance mode", "QA"),
    APPHANCE_LOG_EVENTS("apphance.log.events", "Log events from standard widgets (one of [true,false]", "false"),
    public static final String DESCRIPTION = 'Apphance properties'

    final String propertyName
    final String description
    final String defaultValue

    ApphanceProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }

}
