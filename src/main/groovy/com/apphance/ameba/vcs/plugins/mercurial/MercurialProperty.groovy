package com.apphance.ameba.vcs.plugins.mercurial

/**
 * Properties for Mercurial plugin.
 *
 */
enum MercurialProperty {
    COMMIT_USER("hg.commit.user", "Commit user for mercurial - usually in form of \"Name <e-mail>\"")
    public static final String DESCRIPTION = "Mercurial properties"
    private final String propertyName
    private final String description
    private final String defaultValue

    MercurialProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
