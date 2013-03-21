package com.apphance.ameba.plugins.ios.buildplugin

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