package com.apphance.ameba.plugins.release

/**
 * Properties for all release-related plugins.
 *
 */
enum ProjectReleaseProperty {
    RELEASE_PROJECT_ICON_FILE('release.project.icon.file', 'Path to project\'s icon file'),
    RELEASE_PROJECT_URL('release.project.url', 'Base project URL where the artifacts will be placed. This should be folder URL where last element (after last /) is used as subdirectory of ota dir when artifacts are created locally.'),
    RELEASE_PROJECT_LANGUAGE('release.project.language', 'Language of the project', 'en'),
    RELEASE_PROJECT_COUNTRY('release.project.country', 'Project country', 'US'),
    RELEASE_MAIL_FROM('release.mail.from', 'Sender email address'),
    RELEASE_MAIL_TO('release.mail.to', 'Recipient of release email'),
    RELEASE_MAIL_FLAGS('release.mail.flags', 'Flags for release email', 'qrCode,imageMontage');

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
