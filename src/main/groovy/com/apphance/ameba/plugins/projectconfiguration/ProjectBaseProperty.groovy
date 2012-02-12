package com.apphance.ameba.plugins.projectconfiguration


enum ProjectBaseProperty {

    PROJECT_NAME('project.name', 'Name of the project'),
    PROJECT_ICON_FILE("project.icon.file", 'Path to project icon file'),
    PROJECT_URL('project.url.base', 'Project url'),
    PROJECT_DIRECTORY('project.directory.name', 'Name of project directory'),
    PROJECT_LANGUAGE('project.language', 'Language of project'),
    PROJECT_COUNTRY('project.country', 'Project country');

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
