package com.apphance.ameba.plugins.release


enum ProjectReleaseProperty {

    RELEASE_MAIL_FROM('release.mail.from', 'Sender email address'),
    RELEASE_MAIL_TO('release.mail.to', 'Recipient of release email'),
    RELEASE_MAIL_FLAGS('release.mail.flags', 'Flags for release email');

    public static final DESCRIPTION = 'Release properties'

    final String propertyName
    final String description
    final String defaultValue

    ProjectReleaseProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
