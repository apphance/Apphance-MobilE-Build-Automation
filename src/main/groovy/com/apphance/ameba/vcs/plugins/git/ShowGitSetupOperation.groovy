package com.apphance.ameba.vcs.plugins.git

import com.apphance.ameba.AbstractShowSetupOperation;

/**
 * Shows Git-related properties.
 *
 */
class ShowGitSetupOperation extends AbstractShowSetupOperation {
    ShowGitSetupOperation() {
        super(GitProperty.class)
    }
}

