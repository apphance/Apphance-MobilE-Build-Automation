package com.apphance.ameba.plugins.projectconfiguration


enum ProjectBaseProperty {

    PROJECT_NAME(false, 'project.name', 'Name of the project'),
    PROJECT_ICON_FILE(false, "project.icon.file", 'Path to project icon file'),
    PROJECT_URL(false, 'project.url.base', 'Project url'),
    PROJECT_DIRECTORY(false, 'project.directory.name', 'Name of project directory'),
    PROJECT_LANGUAGE(false, 'project.language', 'Language of project'),
    PROJECT_COUNTRY(false, 'project.country', 'Project country');

    public static final DESCRIPTION = "Base properties"
    private final boolean optional
    private final String propertyName
    private final String description

    ProjectBaseProperty(boolean optional, String proeprtyName, String description) {
        this.optional = optional
        this.propertyName = name
        this.description = description
    }
}
