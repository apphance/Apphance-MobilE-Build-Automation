package com.apphance.ameba.vcs.plugins.mercurial

import com.apphance.ameba.AbstractShowSetupOperation;

/**
 * Shows Mercurial-related properties.
 *
 */
class ShowMercurialSetupOperation extends AbstractShowSetupOperation {
    ShowMercurialSetupOperation() {
        super(MercurialProperty.class)
    }
}

