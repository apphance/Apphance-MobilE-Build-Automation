package com.apphance.ameba.apphance

import com.apphance.ameba.AbstractShowSetupOperation

/**
 * Shows properties for Apphance integration.
 *
 */
class ShowApphancePropertiesOperation extends AbstractShowSetupOperation {
    public ShowApphancePropertiesOperation() {
        super(ApphanceProperty.class)
    }
}
