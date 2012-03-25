package com.apphance.ameba.android.plugins.test

import com.apphance.ameba.AbstractShowSetupOperation;

/**
 * Shows configuration for android tests.
 *
 */
class ShowAndroidTestSetupOperation extends AbstractShowSetupOperation {
    ShowAndroidTestSetupOperation() {
        super(AndroidTestProperty.class)
    }
}