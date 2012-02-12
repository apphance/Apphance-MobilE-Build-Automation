package com.apphance.ameba.plugins.projectconfiguration


enum ProjectBaseProperty {
    PROJECT_ICON_FILE("project.icon.file", 'Path to project\'s icon file'),
    PROJECT_URL('project.url.base', 'Base project URL where the artifacts will be available when released (for example http://example.com/)',''),
    PROJECT_DIRECTORY('project.directory.name', 'Name of subdirectory (at base url) where the artifacts will be placed (for example "testproject" leads to http://example.com/testproject)'),
    PROJECT_LANGUAGE('project.language', 'Language of the project','en'),
    PROJECT_COUNTRY('project.country', 'Project country','US');

    public static final DESCRIPTION = "Base properties"
    private final String propertyName
    private final String description
    private final String defaultValue

    ProjectBaseProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
