package com.apphance.ameba.vcs.plugins.mercurial

enum MercurialProperty {
    COMMIT_USER(false, "hg.commit.user", "Mail of commit user")
    public static final String DESCRIPTION = "Mercurial properties"
    private final boolean optional
    private final String propertyName
    private final String description
    private final String defaultValue

    MercurialProperty(boolean optional, String propertyName, String description, String defaultValue = null) {
        this.optional = optional
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
