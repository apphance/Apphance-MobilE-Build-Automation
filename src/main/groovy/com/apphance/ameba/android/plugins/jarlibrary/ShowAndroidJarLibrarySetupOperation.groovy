package com.apphance.ameba.android.plugins.jarlibrary

import com.apphance.ameba.AbstractShowSetupOperation;

/**
 * Shows all android jar library properties.
 *
 */
class ShowAndroidJarLibrarySetupOperation extends AbstractShowSetupOperation {
    ShowAndroidJarLibrarySetupOperation() {
        super(AndroidJarLibraryProperty.class)
    }
}