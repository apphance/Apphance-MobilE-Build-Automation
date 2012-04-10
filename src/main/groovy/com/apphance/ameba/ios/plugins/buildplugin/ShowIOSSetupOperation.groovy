package com.apphance.ameba.ios.plugins.buildplugin

import com.apphance.ameba.AbstractShowSetupOperation;

/**
 * Shows IOS properties.
 *
 */
class ShowIOSSetupOperation extends AbstractShowSetupOperation {
    ShowIOSSetupOperation() {
        super(IOSProjectProperty.class)
    }
}