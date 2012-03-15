package com.apphance.ameba.plugins.release


enum ProjectReleaseProperty {
    RELEASE_PROJECT_ICON_FILE('release.project.icon.file', 'Path to project\'s icon file'),
    RELEASE_PROJECT_URL('release.project.url.base', 'Base project URL where the artifacts will be available when released (for example http://example.com/)'),
    RELEASE_PROJECT_DIRECTORY('release.project.directory.name', 'Name of subdirectory (at base url) where the artifacts will be placed (for example "testproject" leads to http://example.com/testproject)'),
    RELEASE_PROJECT_LANGUAGE('release.project.language', 'Language of the project','en'),
    RELEASE_PROJECT_COUNTRY('release.project.country', 'Project country','US'),
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
