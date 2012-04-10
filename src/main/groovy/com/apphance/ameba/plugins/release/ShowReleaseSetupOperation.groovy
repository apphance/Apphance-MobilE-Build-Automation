package com.apphance.ameba.plugins.release

import com.apphance.ameba.AbstractShowSetupOperation;

/**
 * Shows all release-specific properties.
 *
 */
class ShowReleaseSetupOperation extends AbstractShowSetupOperation {
    ShowReleaseSetupOperation() {
        super(ProjectReleaseProperty.class)
    }
}

