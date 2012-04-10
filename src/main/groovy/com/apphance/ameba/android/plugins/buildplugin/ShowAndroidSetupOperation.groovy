package com.apphance.ameba.android.plugins.buildplugin

import com.apphance.ameba.AbstractShowSetupOperation;

/**
 * Shows android properties.
 *
 */
class ShowAndroidSetupOperation extends AbstractShowSetupOperation {
    ShowAndroidSetupOperation() {
        super(AndroidProjectProperty.class)
    }
}