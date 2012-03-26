package com.apphance.ameba.vcs.plugins.git

/**
 * Git-related properties.
 *
 */
enum GitProperty {
    GIT_BRANCH("git.branch", "Branch which is used for git operations", 'master')
    public static final String DESCRIPTION = "Git properties"
    private final String propertyName
    private final String description
    private final String defaultValue

    GitProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
