package com.apphance.ameba.plugins.ios.framework

import com.apphance.ameba.AbstractShowSetupOperation;

/**
 * Shows all properties for framework preparation.
 *
 */
class ShowFrameworkSetupOperation extends AbstractShowSetupOperation {
    ShowFrameworkSetupOperation() {
        super(IOSFrameworkProperty.class)
    }
}

