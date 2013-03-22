package com.apphance.ameba.plugins.android.buildplugin

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