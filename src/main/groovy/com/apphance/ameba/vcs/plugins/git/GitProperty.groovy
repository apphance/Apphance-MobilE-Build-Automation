package com.apphance.ameba.vcs.plugins.git

enum GitProperty {
    COMMIT_USER(false, "git.branch", "Branch which is used for git operations", 'master')
    public static final String DESCRIPTION = "Git properties"
    private final boolean optional
    private final String propertyName
    private final String description
    private final String defaultValue

    GitProperty(boolean optional, String propertyName, String description, String defaultValue = null) {
        this.optional = optional
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
