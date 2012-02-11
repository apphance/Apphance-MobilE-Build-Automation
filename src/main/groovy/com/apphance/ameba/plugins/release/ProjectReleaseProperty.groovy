package com.apphance.ameba.plugins.release


enum ProjectReleaseProperty {

    RELEASE_MAIL_FROM(false, 'release.mail.from', 'Sender email address'),
    RELEASE_MAIL_TO(false, 'release.mail.to', 'Recipient of release email'),
    RELEASE_MAIL_FLAGS(false, 'release.mail.flags', 'Flags for release email');

    public static final DESCRIPTION = 'Release properties'

    private final boolean optional
    private final String propertyName
    private final String description

    ProjectReleaseProperty(boolean optional, String propertyName, String description) {
        this.optional = optional
        this.propertyName = propertyName
        this.description = description
    }
}
